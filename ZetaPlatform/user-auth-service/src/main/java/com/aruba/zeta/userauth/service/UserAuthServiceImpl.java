package com.aruba.zeta.userauth.service;

import com.aruba.zeta.userauth.client.UserMgmtClient;
import com.aruba.zeta.userauth.entity.AuthUserEntity;
import com.aruba.zeta.userauth.repository.AuthUserRepo;
import com.aruba.zeta.userauth.repository.ServiceTokenRepo;
import com.aruba.zeta.userauth.grpc.DeleteUserRequest;
import com.aruba.zeta.userauth.grpc.DeleteUserResponse;
import com.aruba.zeta.userauth.grpc.LoginRequest;
import com.aruba.zeta.userauth.grpc.LoginResponse;
import com.aruba.zeta.userauth.grpc.RegisterUserRequest;
import com.aruba.zeta.userauth.grpc.RegisterUserResponse;
import com.aruba.zeta.userauth.grpc.UserAuthServiceGrpc;
import com.aruba.zeta.userauth.grpc.ValidateTokenRequest;
import com.aruba.zeta.userauth.grpc.ValidateTokenResponse;
import io.grpc.stub.StreamObserver;
import it.aruba.zeta.user.grpc.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserAuthServiceImpl extends UserAuthServiceGrpc.UserAuthServiceImplBase {

    private final AuthUserRepo authUserRepo;
    private final ServiceTokenRepo serviceTokenRepo;
    private final UserMgmtClient userMgmtClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        log.debug("Attempting login for user: {}", username);

        try {
            Optional<AuthUserEntity> authUserOpt = authUserRepo.findByUsername(username);

            if (authUserOpt.isEmpty()) {
                log.warn("Login failed: User {} not found in AuthRepo", username);
                sendLoginError(responseObserver, "Invalid credentials");
                return;
            }

            AuthUserEntity authUser = authUserOpt.get();

            if (!passwordEncoder.matches(password, authUser.getPasswordHash())) {
                log.warn("Login failed: Invalid password for user {}", username);
                sendLoginError(responseObserver, "Invalid credentials");
                return;
            }

            String userId = authUser.getUserId().toString();
            UserResponse userMgmtResponse;
            try {
                userMgmtResponse = userMgmtClient.getUser(userId);
            } catch (Exception e) {
                log.error("Login failed: User {} found in AuthRepo but check against UserMgmtService failed", username, e);
                sendLoginError(responseObserver, "User verification failed");
                return;
            }

            if (!userMgmtResponse.getUser().getIsActive()) {
                log.warn("Login failed: User {} is inactive", username);
                sendLoginError(responseObserver, "User is inactive");
                return;
            }

            String token = jwtTokenService.generateToken(userId);

            LoginResponse response = LoginResponse.newBuilder()
                    .setSuccess(true)
                    .setUserId(userId)
                    .setInternalToken(token)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("User {} logged in successfully", username);

        } catch (Exception e) {
            log.error("Unexpected error during login for user {}", username, e);
            sendLoginError(responseObserver, "Internal server error");
        }
    }

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        String token = request.getInternalToken();

        try {
            if (!jwtTokenService.validateToken(token)) {
                log.warn("Token validation failed: Invalid token");
                sendInvalidToken(responseObserver);
                return;
            }


            String userId = jwtTokenService.extractUserId(token);

            if (userId == null) {
                sendInvalidToken(responseObserver);
                return;
            }

            Optional<AuthUserEntity> authUserOpt = authUserRepo.findByUserId(UUID.fromString(userId));
            if (authUserOpt.isEmpty()) {
                log.warn("Token validation failed: User {} no longer exists in AuthRepo", userId);
                sendInvalidToken(responseObserver);
                return;
            }

            UserResponse userMgmtResponse;
            try {
                userMgmtResponse = userMgmtClient.getUser(userId);
            } catch (Exception e) {
                log.warn("Token validation failed: User {} check against UserMgmtService failed", userId, e);
                sendInvalidToken(responseObserver);
                return;
            }

            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setIsValid(true)
                    .setUserId(userId)
                    .setRole(userMgmtResponse.getUser().getRole())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Unexpected error during token validation", e);
            sendInvalidToken(responseObserver);
        }
    }

    @Override
    public void registerUser(RegisterUserRequest request, StreamObserver<RegisterUserResponse> responseObserver) {
        String username = request.getUsername();

        log.debug("Attempting to register user: {}", username);

        try {
            if (authUserRepo.findByUsername(username).isPresent()) {
                log.warn("Registration failed: username {} already exists", username);
                sendRegisterError(responseObserver, "Username already exists");
                return;
            }

            UserResponse createResponse;
            try {
                createResponse = userMgmtClient.createUser(
                        username,
                        request.getEmail(),
                        request.getFirstName(),
                        request.getLastName(),
                        request.getRole(),
                        request.getSemanticIndexingEnabled()
                );
            } catch (Exception e) {
                log.error("Registration failed: could not create user {} in UserMgmtService", username, e);
                sendRegisterError(responseObserver, "User creation failed");
                return;
            }

            String userId = createResponse.getUser().getId();
            String passwordHash = passwordEncoder.encode(request.getPassword());

            AuthUserEntity authUser = AuthUserEntity.builder()
                    .userId(UUID.fromString(userId))
                    .username(username)
                    .passwordHash(passwordHash)
                    .build();

            authUserRepo.save(authUser);

            RegisterUserResponse response = RegisterUserResponse.newBuilder()
                    .setSuccess(true)
                    .setUserId(userId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("User {} registered successfully with id {}", username, userId);

        } catch (Exception e) {
            log.error("Unexpected error during registration for user {}", username, e);
            sendRegisterError(responseObserver, "Internal server error");
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        String userIdStr = request.getUserId();

        log.debug("Attempting to delete user with id: {}", userIdStr);

        try {
            UUID userId = UUID.fromString(userIdStr);

            if (authUserRepo.findByUserId(userId).isEmpty()) {
                log.warn("Delete failed: user {} not found in AuthRepo", userIdStr);
                sendDeleteError(responseObserver, "User not found");
                return;
            }

            serviceTokenRepo.deleteAllByUserId(userId);
            authUserRepo.deleteById(authUserRepo.findByUserId(userId).get().getId());

            try {
                userMgmtClient.deleteUser(userIdStr);
            } catch (Exception e) {
                log.error("Delete: auth credentials removed but UserMgmtService deletion failed for user {}", userIdStr, e);
                sendDeleteError(responseObserver, "Credentials deleted but user profile removal failed");
                return;
            }

            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("User {} deleted successfully", userIdStr);

        } catch (IllegalArgumentException e) {
            log.warn("Delete failed: invalid user id format '{}'", userIdStr);
            sendDeleteError(responseObserver, "Invalid user id format");
        } catch (Exception e) {
            log.error("Unexpected error during deletion of user {}", userIdStr, e);
            sendDeleteError(responseObserver, "Internal server error");
        }
    }

    private void sendLoginError(StreamObserver<LoginResponse> observer, String message) {
        observer.onNext(LoginResponse.newBuilder()
                .setSuccess(false)
                .setErrorMessage(message)
                .build());
        observer.onCompleted();
    }

    private void sendRegisterError(StreamObserver<RegisterUserResponse> observer, String message) {
        observer.onNext(RegisterUserResponse.newBuilder()
                .setSuccess(false)
                .setErrorMessage(message)
                .build());
        observer.onCompleted();
    }

    private void sendInvalidToken(StreamObserver<ValidateTokenResponse> observer) {
        observer.onNext(ValidateTokenResponse.newBuilder()
                .setIsValid(false)
                .build());
        observer.onCompleted();
    }

    private void sendDeleteError(StreamObserver<DeleteUserResponse> observer, String message) {
        observer.onNext(DeleteUserResponse.newBuilder()
                .setSuccess(false)
                .setErrorMessage(message)
                .build());
        observer.onCompleted();
    }
}
