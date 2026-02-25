package com.aruba.zeta.userauth.service;

import com.aruba.zeta.integrtoken.grpc.GetServiceTokenRequest;
import com.aruba.zeta.integrtoken.grpc.GetServiceTokenResponse;
import com.aruba.zeta.integrtoken.grpc.IntegrationTokenServiceGrpc;
import com.aruba.zeta.integrtoken.grpc.SaveServiceTokenRequest;
import com.aruba.zeta.integrtoken.grpc.SaveServiceTokenResponse;
import com.aruba.zeta.userauth.entity.ServiceTokenEntity;
import com.aruba.zeta.userauth.enums.IntegrationServiceType;
import com.aruba.zeta.userauth.repository.ServiceTokenRepo;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * gRPC service implementation for managing encrypted OAuth2 integration tokens.
 * Handles secure persistence and retrieval of third-party service token pairs.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class IntegrationTokenServiceImpl extends IntegrationTokenServiceGrpc.IntegrationTokenServiceImplBase {

    private final ServiceTokenRepo serviceTokenRepo;
    private final AesGcmEncryptionService encryptionService;

    /**
     * Persists an OAuth2 token pair for a given user and service type.
     */
    @Override
    public void saveServiceToken(SaveServiceTokenRequest request, StreamObserver<SaveServiceTokenResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            IntegrationServiceType serviceType = mapServiceType(request.getServiceType());

            String encryptedAccessToken = encryptionService.encrypt(request.getAccessToken());
            String encryptedRefreshToken = request.getRefreshToken().isBlank()
                    ? null
                    : encryptionService.encrypt(request.getRefreshToken());

            Instant expiresAt = Instant.ofEpochSecond(request.getExpiresAt());

            // Upsert: update existing record or build a new one
            Optional<ServiceTokenEntity> existing = serviceTokenRepo.findByUserIdAndServiceType(userId, serviceType);
            ServiceTokenEntity entity = existing.orElseGet(() -> ServiceTokenEntity.builder()
                    .userId(userId)
                    .serviceType(serviceType)
                    .build());

            entity.setEncryptedAccessToken(encryptedAccessToken);
            entity.setEncryptedRefreshToken(encryptedRefreshToken);
            entity.setTokenExpiresAt(expiresAt);

            serviceTokenRepo.save(entity);
            log.info("Saved {} token for user {}", serviceType, userId);

            responseObserver.onNext(SaveServiceTokenResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Token saved successfully")
                    .build());
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            log.error("Invalid request in saveServiceToken: {}", e.getMessage());
            responseObserver.onNext(SaveServiceTokenResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Invalid request: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Unexpected error in saveServiceToken for user {}", request.getUserId(), e);
            responseObserver.onNext(SaveServiceTokenResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Internal server error")
                    .build());
            responseObserver.onCompleted();
        }
    }

    /**
     * Retrieves a decrypted OAuth2 token pair for a given user and service type.
     * Tokens are decrypted on-the-fly in memory; plaintext is never persisted or logged.
     */
    @Override
    public void getServiceToken(GetServiceTokenRequest request, StreamObserver<GetServiceTokenResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            IntegrationServiceType serviceType = mapServiceType(request.getServiceType());

            Optional<ServiceTokenEntity> tokenOpt = serviceTokenRepo.findByUserIdAndServiceType(userId, serviceType);

            if (tokenOpt.isEmpty()) {
                log.debug("No {} token found for user {}", serviceType, userId);
                responseObserver.onNext(GetServiceTokenResponse.newBuilder()
                        .setFound(false)
                        .build());
                responseObserver.onCompleted();
                return;
            }

            ServiceTokenEntity entity = tokenOpt.get();

            String accessToken = encryptionService.decrypt(entity.getEncryptedAccessToken());
            String refreshToken = entity.getEncryptedRefreshToken() != null
                    ? encryptionService.decrypt(entity.getEncryptedRefreshToken())
                    : "";

            responseObserver.onNext(GetServiceTokenResponse.newBuilder()
                    .setFound(true)
                    .setAccessToken(accessToken)
                    .setRefreshToken(refreshToken)
                    .setExpiresAt(entity.getTokenExpiresAt().getEpochSecond())
                    .build());
            responseObserver.onCompleted();
            log.info("Retrieved {} token for user {}", serviceType, userId);

        } catch (IllegalArgumentException e) {
            log.error("Invalid request in getServiceToken: {}", e.getMessage());
            responseObserver.onNext(GetServiceTokenResponse.newBuilder()
                    .setFound(false)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Unexpected error in getServiceToken for user {}", request.getUserId(), e);
            responseObserver.onNext(GetServiceTokenResponse.newBuilder()
                    .setFound(false)
                    .build());
            responseObserver.onCompleted();
        }
    }

    private IntegrationServiceType mapServiceType(com.aruba.zeta.integrtoken.grpc.IntegrationServiceType protoType) {
        return switch (protoType) {
            case SERVICE_TYPE_PEC -> IntegrationServiceType.PEC;
            case SERVICE_TYPE_SIGN -> IntegrationServiceType.SIGN;
            case SERVICE_TYPE_CONSERVATION -> IntegrationServiceType.CONSERVATION;
            default -> throw new IllegalArgumentException("Unsupported or unspecified service type: " + protoType);
        };
    }
}
