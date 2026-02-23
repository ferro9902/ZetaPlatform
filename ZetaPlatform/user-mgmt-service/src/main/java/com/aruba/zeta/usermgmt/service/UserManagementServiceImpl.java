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

import com.aruba.zeta.usermgmt.entity.UserEntity;
import com.aruba.zeta.usermgmt.repository.UserRepo;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.grpc.server.service.GrpcService;

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

        UserEntity savedEntity = userRepository.save(entity);

        UserResponse response = UserResponse.newBuilder()
                .setUser(mapToGrpcUser(savedEntity))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        userRepository.findById(UUID.fromString(request.getId()))
                .ifPresentOrElse(
                        user -> {
                            UserResponse response = UserResponse.newBuilder()
                                    .setUser(mapToGrpcUser(user))
                                    .build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(io.grpc.Status.NOT_FOUND
                                .withDescription("User not found")
                                .asRuntimeException())
                );
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
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
                            user.setSemanticIndexingEnabled(request.getSemanticIndexingEnabled());

                            UserEntity savedEntity = userRepository.save(user);
                            UserResponse response = UserResponse.newBuilder()
                                    .setUser(mapToGrpcUser(savedEntity))
                                    .build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(io.grpc.Status.NOT_FOUND
                                .withDescription("User not found")
                                .asRuntimeException())
                );
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        UUID id = UUID.fromString(request.getId());
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User deleted successfully")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("User not found")
                    .asRuntimeException());
        }
    }

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