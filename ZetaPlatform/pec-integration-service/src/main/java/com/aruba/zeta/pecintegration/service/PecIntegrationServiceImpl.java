package com.aruba.zeta.pecintegration.service;

import java.time.Instant;
import java.util.List;

import org.springframework.grpc.server.service.GrpcService;

import com.aruba.zeta.pec.grpc.PecIntegrationServiceGrpc.PecIntegrationServiceImplBase;
import com.aruba.zeta.pecintegration.client.IntegrationTokenClient;
import com.aruba.zeta.pecintegration.client.rest.ArubaPecApiClient;
import com.aruba.zeta.pecintegration.client.rest.ArubaPecOAuth2Client;
import com.aruba.zeta.pecintegration.dto.ArubaMailboxDto;
import com.aruba.zeta.pecintegration.dto.ArubaMessagePage;
import com.aruba.zeta.pecintegration.dto.ArubaSendMessageRequest;
import com.aruba.zeta.pecintegration.dto.ArubaSendMessageResponse;
import com.aruba.zeta.pecintegration.dto.ArubaTokenResponse;
import com.aruba.zeta.pecintegration.dto.ServiceTokenDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Unified service implementation for PEC Integration.
 * Handles PEC messaging proxy operations and manages Aruba PEC OAuth2 token lifecycle.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class PecIntegrationServiceImpl extends PecIntegrationServiceImplBase {

    // REST clients
    private final ArubaPecApiClient arubaPecApiClient;
    private final ArubaPecOAuth2Client arubaPecOAuth2Client;
    // gRPC clients
    private final IntegrationTokenClient integrationTokenClient;

    /**
     * Retrieves all PEC mailboxes accessible via the given token.
     *
     * @param accessToken OAuth2 access token
     * @return list of mailboxes
     */
    public List<ArubaMailboxDto> getMailboxes(String accessToken) {
        log.debug("Proxying mailbox list request");
        return arubaPecApiClient.getMailboxes(accessToken);
    }

    /**
     * Retrieves messages directly from the external provider.
     *
     * @param accessToken OAuth2 access token
     * @param mailboxId   The mailbox identifier
     * @param page        Page number (0-based)
     * @param size        Page size
     * @return Page of messages
     */
    public ArubaMessagePage getMessages(String accessToken, String mailboxId, int page, int size) {
        log.debug("Proxying message request for mailbox {} (page={}, size={})", mailboxId, page, size);
        return arubaPecApiClient.getMessages(accessToken, mailboxId, page, size);
    }

    /**
     * Submits an outbound PEC message to the external provider.
     *
     * @param accessToken OAuth2 access token
     * @param mailboxId   Sender mailbox identifier
     * @param request     Message payload
     * @return submission acknowledgement
     */
    public ArubaSendMessageResponse sendMessage(String accessToken, String mailboxId, ArubaSendMessageRequest request) {
        log.debug("Proxying send-message request from mailbox {} to {}", mailboxId, request.getTo());
        return arubaPecApiClient.sendMessage(accessToken, mailboxId, request);
    }

    // Token management

    // Account linking (called at OAuth2 callback)

    /**
     * Exchanges the authorization code for a token pair and persists it.
     *
     * @param userId   the platform user UUID
     * @param authCode the authorization code from the redirect URI
     * @throws PecTokenException if exchange or persistence fails
     */
    public void linkPecAccount(String userId, String authCode) {
        log.info("Linking PEC account for user {}", userId);

        ArubaTokenResponse tokenResponse = arubaPecOAuth2Client.exchangeAuthorizationCode(authCode);
        persistTokens(userId, tokenResponse);

        log.info("PEC account successfully linked for user {}", userId);
    }

    // Token retrieval with transparent refresh

    /**
     * Retrieves a valid access token for the user, refreshing it automatically if expired.
     *
     * @param userId the platform user UUID
     * @return a valid plaintext access token
     * @throws PecTokenException if the account is not linked or refresh fails
     */
    public String getValidAccessToken(String userId) {
        log.debug("Resolving valid PEC access token for user {}", userId);

        ServiceTokenDto stored = integrationTokenClient.getServiceToken(userId);

        if (!stored.isFound()) {
            throw new PecTokenException(
                    "No PEC token found for user " + userId + ". Account not linked.");
        }

        if (isExpired(stored.getExpiresAt())) {
            log.info("PEC access token expired for user {}; refreshing", userId);
            return refreshAndPersist(userId, stored.getRefreshToken());
        }

        return stored.getAccessToken();
    }

    // Internal helpers

    /**
     * Refreshes the access token and persists the new token pair.
     */
    private String refreshAndPersist(String userId, String refreshToken) {
        ArubaTokenResponse refreshed = arubaPecOAuth2Client.refreshAccessToken(refreshToken);
        persistTokens(userId, refreshed);
        log.info("PEC access token refreshed and stored for user {}", userId);
        return refreshed.getAccessToken();
    }

    /**
     * Persists the token pair to the user-auth-service.
     */
    private void persistTokens(String userId, ArubaTokenResponse tokenResponse) {
        long expiresAt = Instant.now().getEpochSecond() + tokenResponse.getExpiresIn();

        integrationTokenClient.saveServiceToken(
                userId,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                expiresAt);
    }

    /**
     * Checks if the token is expired or will expire within 60 seconds.
     */
    private boolean isExpired(long expiresAtEpochSecond) {
        return Instant.now().getEpochSecond() >= expiresAtEpochSecond - 60;
    }

    // Exception type

    /** Signals a failure in any PEC token operation. */
    public static class PecTokenException extends RuntimeException {
        public PecTokenException(String message) {
            super(message);
        }
    }
}
