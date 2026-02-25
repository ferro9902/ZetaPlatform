package com.aruba.zeta.usermgmt.service;

import io.grpc.stub.StreamObserver;
import it.aruba.zeta.user.grpc.CreateUserRequest;
import it.aruba.zeta.user.grpc.DeleteUserRequest;
import it.aruba.zeta.user.grpc.DeleteUserResponse;
import it.aruba.zeta.user.grpc.GetUserRequest;
import it.aruba.zeta.user.grpc.UpdateUserRequest;
import it.aruba.zeta.user.grpc.User;
import it.aruba.zeta.user.grpc.UserManagementServiceGrpc.UserManagementServiceImplBase;
import it.aruba.zeta.user.grpc.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.aruba.zeta.usermgmt.entity.UserEntity;
import com.aruba.zeta.usermgmt.repository.UserRepo;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.grpc.server.service.GrpcService;

/**
 * gRPC service implementation for user management operations.
 * Handles creation, retrieval, update, and deletion of user profiles.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserManagementServiceImpl extends UserManagementServiceImplBase {

    private final UserRepo userRepository;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        UserEntity entity = new UserEntity();
        entity.setUsername(request.getUsername());
        entity.setEmail(request.getEmail());
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setSemanticIndexingEnabled(request.getSemanticIndexingEnabled());
        if (!request.getRole().isEmpty()) {
            entity.setRole(request.getRole());
        }

        log.debug("Creating user with username: {}", request.getUsername());
        UserEntity savedEntity = userRepository.save(entity);

        UserResponse response = UserResponse.newBuilder()
                .setUser(mapToGrpcUser(savedEntity))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        log.info("User {} created successfully with id {}", savedEntity.getUsername(), savedEntity.getId());
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        log.debug("Retrieving user with id: {}", request.getId());
        userRepository.findById(UUID.fromString(request.getId()))
                .ifPresentOrElse(
                        user -> {
                            UserResponse response = UserResponse.newBuilder()
                                    .setUser(mapToGrpcUser(user))
                                    .build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        () -> {
                            log.warn("Get failed: user {} not found", request.getId());
                            responseObserver.onError(io.grpc.Status.NOT_FOUND
                                    .withDescription("User not found")
                                    .asRuntimeException());
                        }
                );
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        log.debug("Updating user with id: {}", request.getId());
        userRepository.findById(UUID.fromString(request.getId()))
                .ifPresentOrElse(
                        user -> {
                            if (!request.getFirstName().isEmpty()) {
                                user.setFirstName(request.getFirstName());
                            }
                            if (!request.getLastName().isEmpty()) {
                                user.setLastName(request.getLastName());
                            }
                            if (!request.getEmail().isEmpty()) {
                                user.setEmail(request.getEmail());
                            }
                            if (!request.getUsername().isEmpty()) {
                                user.setUsername(request.getUsername());
                            }
                            if (!request.getRole().isEmpty()) {
                                user.setRole(request.getRole());
                            }
                            if (request.hasIsActive()) {
                                user.setActive(request.getIsActive().getValue());
                            }
                            if (request.hasSemanticIndexingEnabled()) {
                                user.setSemanticIndexingEnabled(request.getSemanticIndexingEnabled().getValue());
                            }

                            UserEntity savedEntity = userRepository.save(user);
                            UserResponse response = UserResponse.newBuilder()
                                    .setUser(mapToGrpcUser(savedEntity))
                                    .build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                            log.info("User {} updated successfully", savedEntity.getId());
                        },
                        () -> {
                            log.warn("Update failed: user {} not found", request.getId());
                            responseObserver.onError(io.grpc.Status.NOT_FOUND
                                    .withDescription("User not found")
                                    .asRuntimeException());
                        }
                );
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        log.debug("Deleting user with id: {}", request.getId());
        UUID id = UUID.fromString(request.getId());
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User deleted successfully")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("User {} deleted successfully", id);
        } else {
            log.warn("Delete failed: user {} not found", id);
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("User not found")
                    .asRuntimeException());
        }
    }

    /**
     * Maps a {@link UserEntity} to the gRPC {@link User} proto message.
     *
     * @param entity the JPA entity to convert
     * @return the corresponding gRPC {@code User} proto message
     */
    private User mapToGrpcUser(UserEntity entity) {
        return User.newBuilder()
                .setId(entity.getId().toString())
                .setUsername(entity.getUsername())
                .setEmail(entity.getEmail())
                .setFirstName(entity.getFirstName())
                .setLastName(entity.getLastName())
                .setRole(entity.getRole())
                .setIsActive(entity.isActive())
                .setSemanticIndexingEnabled(entity.isSemanticIndexingEnabled())
                .setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
                .setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
                .build();
    }

}