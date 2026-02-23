package com.aruba.zeta.pecintegration.client;

import org.springframework.stereotype.Component;

import com.aruba.zeta.integrtoken.grpc.GetServiceTokenRequest;
import com.aruba.zeta.integrtoken.grpc.GetServiceTokenResponse;
import com.aruba.zeta.integrtoken.grpc.IntegrationServiceType;
import com.aruba.zeta.integrtoken.grpc.IntegrationTokenServiceGrpc;
import com.aruba.zeta.integrtoken.grpc.SaveServiceTokenRequest;
import com.aruba.zeta.integrtoken.grpc.SaveServiceTokenResponse;
import com.aruba.zeta.pecintegration.dto.ServiceTokenDto;

import lombok.extern.slf4j.Slf4j;

/**
 * gRPC client for the {@code IntegrationTokenService} exposed by
 * {@code user-auth-service}. Handles secure retrieval and persistence of
 * OAuth2 tokens.
 */
@Slf4j
@Component
public class IntegrationTokenClient {

    private final IntegrationTokenServiceGrpc.IntegrationTokenServiceBlockingStub stub;

    public IntegrationTokenClient(IntegrationTokenServiceGrpc.IntegrationTokenServiceBlockingStub stub) {
        this.stub = stub;
    }

    // Token retrieval

    /**
     * Retrieves the decrypted PEC OAuth2 token for the given user.
     *
     * @param userId the platform user UUID
     * @return domain DTO containing the token if found
     */
    public ServiceTokenDto getServiceToken(String userId) {
        log.debug("Requesting PEC service token for user {}", userId);
        GetServiceTokenRequest request = GetServiceTokenRequest.newBuilder()
                .setUserId(userId)
                .setServiceType(IntegrationServiceType.SERVICE_TYPE_PEC)
                .build();
        GetServiceTokenResponse response = stub.getServiceToken(request);

        ServiceTokenDto dto = new ServiceTokenDto();
        dto.setFound(response.getFound());
        dto.setAccessToken(response.getAccessToken());
        dto.setRefreshToken(response.getRefreshToken());
        dto.setExpiresAt(response.getExpiresAt());
        return dto;
    }

    // Token persistence

    /**
     * Persists the Aruba OAuth2 token pair securely via user-auth-service.
     * Throws {@link RuntimeException} if the remote call reports failure.
     *
     * @param userId       the platform user UUID
     * @param accessToken  OAuth2 access token
     * @param refreshToken OAuth2 refresh token
     * @param expiresAt    access token expiration timestamp (epoch seconds)
     */
    public void saveServiceToken(
            String userId,
            String accessToken,
            String refreshToken,
            long expiresAt) {
        log.debug("Saving PEC service token for user {}", userId);
        SaveServiceTokenRequest request = SaveServiceTokenRequest.newBuilder()
                .setUserId(userId)
                .setServiceType(IntegrationServiceType.SERVICE_TYPE_PEC)
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .setExpiresAt(expiresAt)
                .build();
        SaveServiceTokenResponse response = stub.saveServiceToken(request);

        if (!response.getSuccess()) {
            throw new RuntimeException(
                    "Failed to persist PEC token for user " + userId + ": " + response.getMessage());
        }
    }
}
