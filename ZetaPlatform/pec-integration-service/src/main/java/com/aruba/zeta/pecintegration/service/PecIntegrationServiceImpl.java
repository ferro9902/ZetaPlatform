package com.aruba.zeta.pecintegration.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.grpc.server.service.GrpcService;

import com.aruba.zeta.pec.grpc.GetMailboxesRequest;
import com.aruba.zeta.pec.grpc.GetMailboxesResponse;
import com.aruba.zeta.pec.grpc.GetMessagesRequest;
import com.aruba.zeta.pec.grpc.GetMessagesResponse;
import com.aruba.zeta.pec.grpc.LinkPecAccountRequest;
import com.aruba.zeta.pec.grpc.LinkPecAccountResponse;
import com.aruba.zeta.pec.grpc.PecIntegrationServiceGrpc.PecIntegrationServiceImplBase;
import com.aruba.zeta.pec.grpc.SendMessageRequest;
import com.aruba.zeta.pec.grpc.SendMessageResponse;
import io.grpc.stub.StreamObserver;
import com.aruba.zeta.pecintegration.client.IntegrationTokenClient;
import com.aruba.zeta.pecintegration.client.rest.ArubaPecApiClient;
import com.aruba.zeta.pecintegration.client.rest.ArubaPecOAuth2Client;
import com.aruba.zeta.pecintegration.dto.ArubaMailboxDto;
import com.aruba.zeta.pecintegration.dto.ArubaMessagePage;
import com.aruba.zeta.pecintegration.dto.ArubaSendMessageRequest;
import com.aruba.zeta.pecintegration.dto.ArubaSendMessageResponse;
import com.aruba.zeta.pecintegration.dto.ArubaTokenResponse;
import com.aruba.zeta.pecintegration.dto.ServiceTokenDto;
import com.aruba.zeta.pecintegration.mapper.PecProtoMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for PEC Integration.
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

    // gRPC endpoint overrides

    @Override
    public void getMailboxes(GetMailboxesRequest request, StreamObserver<GetMailboxesResponse> responseObserver) {
        log.info("gRPC getMailboxes for user {}", request.getUserId());
        try {
            List<ArubaMailboxDto> mailboxes = fetchMailboxes(request.getUserId());
            GetMailboxesResponse response = GetMailboxesResponse.newBuilder()
                    .addAllMailboxes(mailboxes.stream()
                            .map(PecProtoMapper::toProto)
                            .collect(Collectors.toList()))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to retrieve mailboxes for user {}: {}", request.getUserId(), e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void getMessages(GetMessagesRequest request, StreamObserver<GetMessagesResponse> responseObserver) {
        log.info("gRPC getMessages for user {} mailbox {}", request.getUserId(), request.getMailboxId());
        try {
            Instant startDate = request.hasStartDate()
                    ? Instant.ofEpochSecond(request.getStartDate().getSeconds(), request.getStartDate().getNanos())
                    : null;
            Instant endDate = request.hasEndDate()
                    ? Instant.ofEpochSecond(request.getEndDate().getSeconds(), request.getEndDate().getNanos())
                    : null;
            ArubaMessagePage messagePage = fetchMessages(
                    request.getUserId(), request.getMailboxId(), startDate, endDate);
            String nextPageToken = (messagePage.getPage() + 1 < messagePage.getTotalPages())
                    ? String.valueOf(messagePage.getPage() + 1) : "";
            GetMessagesResponse response = GetMessagesResponse.newBuilder()
                    .addAllMessages(messagePage.getMessages().stream()
                            .map(PecProtoMapper::toProto)
                            .collect(Collectors.toList()))
                    .setNextPageToken(nextPageToken)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to retrieve messages for user {}: {}", request.getUserId(), e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        log.info("gRPC sendMessage for user {} from mailbox {}", request.getUserId(), request.getMailboxId());
        try {
            ArubaSendMessageRequest sendRequest = ArubaSendMessageRequest.builder()
                    .to(request.getRecipientAddress())
                    .subject(request.getSubject())
                    .body(request.getBodyText())
                    .documentIds(request.getDocumentIdsList())
                    .build();
            ArubaSendMessageResponse result = submitMessage(
                    request.getUserId(), request.getMailboxId(), sendRequest);
            responseObserver.onNext(PecProtoMapper.toProto(result));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to send message for user {}: {}", request.getUserId(), e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void linkPecAccount(LinkPecAccountRequest request, StreamObserver<LinkPecAccountResponse> responseObserver) {
        log.info("Linking PEC account for user {}", request.getUserId());
        try {
            ArubaTokenResponse tokenResponse = arubaPecOAuth2Client.exchangeAuthorizationCode(request.getAuthCode());
            persistTokens(request.getUserId(), tokenResponse);
            log.info("PEC account successfully linked for user {}", request.getUserId());
            responseObserver.onNext(LinkPecAccountResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to link PEC account for user {}: {}", request.getUserId(), e.getMessage());
            responseObserver.onError(e);
        }
    }

    private List<ArubaMailboxDto> fetchMailboxes(String userId) {
        log.debug("Proxying mailbox list request for user {}", userId);
        return arubaPecApiClient.getMailboxes(getValidAccessToken(userId));
    }

    private ArubaMessagePage fetchMessages(String userId, String mailboxId, Instant startDate, Instant endDate) {
        log.debug("Proxying message request for mailbox {}", mailboxId);
        return arubaPecApiClient.getMessages(getValidAccessToken(userId), mailboxId, startDate, endDate);
    }

    private ArubaSendMessageResponse submitMessage(String userId, String mailboxId, ArubaSendMessageRequest request) {
        log.debug("Proxying send-message request from mailbox {} to {}", mailboxId, request.getTo());
        return arubaPecApiClient.sendMessage(getValidAccessToken(userId), mailboxId, request);
    }

    /**
     * Retrieves a valid access token for the user, refreshing it automatically if expired.
     *
     * @param userId the platform user UUID
     * @return a valid plaintext access token
     * @throws RuntimeException if the account is not linked or refresh fails
     */
    private String getValidAccessToken(String userId) {
        log.debug("Resolving valid PEC access token for user {}", userId);

        ServiceTokenDto stored = integrationTokenClient.getServiceToken(userId);

        if (!stored.isFound()) {
            throw new RuntimeException(
                    "No PEC token found for user " + userId + ". Account not linked.");
        }

        if (isExpired(stored.getExpiresAt())) {
            log.info("PEC access token expired for user {}; refreshing", userId);
            return refreshAndPersist(userId, stored.getRefreshToken());
        }

        return stored.getAccessToken();
    }

    // Token lifecycle helpers

    private String refreshAndPersist(String userId, String refreshToken) {
        ArubaTokenResponse refreshed = arubaPecOAuth2Client.refreshAccessToken(refreshToken);
        persistTokens(userId, refreshed);
        log.info("PEC access token refreshed and stored for user {}", userId);
        return refreshed.getAccessToken();
    }

    private void persistTokens(String userId, ArubaTokenResponse tokenResponse) {
        long expiresAt = Instant.now().getEpochSecond() + tokenResponse.getExpiresIn();
        integrationTokenClient.saveServiceToken(
                userId,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                expiresAt);
    }

    private boolean isExpired(long expiresAtEpochSecond) {
        return Instant.now().getEpochSecond() >= expiresAtEpochSecond - 60;
    }
}
