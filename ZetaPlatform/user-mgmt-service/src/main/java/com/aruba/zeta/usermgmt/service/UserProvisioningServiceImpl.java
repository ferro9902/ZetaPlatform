package com.aruba.zeta.usermgmt.service;

import io.grpc.stub.StreamObserver;
import it.aruba.zeta.user.grpc.*;

import com.aruba.zeta.usermgmt.entity.UserEntity;
import com.aruba.zeta.usermgmt.repository.UserRepo;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class UserProvisioningServiceImpl extends UserProvisioningServiceGrpc.UserProvisioningServiceImplBase {

    private final UserRepo userRepository;

    public UserProvisioningServiceImpl(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        UserEntity entity = new UserEntity();
        entity.setEmail(request.getEmail());
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
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
                            if (!request.getRole().isEmpty()) {
                                user.setRole(request.getRole());
                            }

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
            DeleteUserResponse response = DeleteUserResponse.newBuilder().build();
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
                .setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
                .build();
    }

}