package com.aruba.zeta.pecintegration.client.rest;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.aruba.zeta.pecintegration.dto.ArubaMailboxDto;
import com.aruba.zeta.pecintegration.dto.ArubaMessagePage;
import com.aruba.zeta.pecintegration.dto.ArubaSendMessageRequest;
import com.aruba.zeta.pecintegration.dto.ArubaSendMessageResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * REST client for the Aruba PEC API.
 * Injects the provided access token into the Authorization header for all requests.
 */
@Slf4j
@Component
public class ArubaPecApiClient {

    private final RestClient restClient;

    public ArubaPecApiClient(@Qualifier("arubaPecApiRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Retrieves all accessible PEC mailboxes.
     *
     * @param accessToken OAuth2 access token
     * @return list of mailboxes
     */
    public List<ArubaMailboxDto> getMailboxes(String accessToken) {
        log.debug("Fetching PEC mailboxes from Aruba");
        return restClient.get()
                .uri("/mailboxes")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // Message operations

    /**
     * Retrieves messages for a mailbox, optionally filtered by date range.
     *
     * @param accessToken OAuth2 access token
     * @param mailboxId   mailbox identifier
     * @param startDate   optional lower bound filter (inclusive)
     * @param endDate     optional upper bound filter (inclusive)
     * @return page of messages
     */
    public ArubaMessagePage getMessages(String accessToken, String mailboxId, @Nullable Instant startDate, @Nullable Instant endDate) {
        log.debug("Fetching messages for mailbox {} (startDate={}, endDate={})", mailboxId, startDate, endDate);
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/mailboxes/{mailboxId}/messages");
                    if (startDate != null) uriBuilder.queryParam("startDate", startDate);
                    if (endDate != null)   uriBuilder.queryParam("endDate", endDate);
                    return uriBuilder.build(mailboxId);
                })
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .retrieve()
                .body(ArubaMessagePage.class);
    }

    /**
     * Submits a new outbound PEC message.
     *
     * @param accessToken OAuth2 access token
     * @param mailboxId   sender mailbox identifier
     * @param request     message payload
     * @return submission acknowledgement
     */
    public ArubaSendMessageResponse sendMessage(String accessToken, String mailboxId, ArubaSendMessageRequest request) {
        log.debug("Sending PEC message from mailbox {} to {}", mailboxId, request.getTo());
        return restClient.post()
                .uri("/mailboxes/{mailboxId}/messages", mailboxId)
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .body(request)
                .retrieve()
                .body(ArubaSendMessageResponse.class);
    }

    // Helpers

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
