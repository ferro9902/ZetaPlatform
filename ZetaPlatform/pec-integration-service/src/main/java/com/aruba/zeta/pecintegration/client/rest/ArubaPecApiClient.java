package com.aruba.zeta.pecintegration.client.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.aruba.zeta.pecintegration.dto.ArubaMailboxDto;
import com.aruba.zeta.pecintegration.dto.ArubaMessageDto;
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
     * Retrieves a paginated list of messages for a mailbox.
     *
     * @param accessToken OAuth2 access token
     * @param mailboxId   mailbox identifier
     * @param page        page index (0-based)
     * @param size        page size
     * @return page of messages
     */
    public ArubaMessagePage getMessages(String accessToken, String mailboxId, int page, int size) {
        log.debug("Fetching messages for mailbox {} (page={}, size={})", mailboxId, page, size);
        return restClient.get()
                .uri("/mailboxes/{mailboxId}/messages?page={page}&size={size}",
                        mailboxId, page, size)
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .retrieve()
                .body(ArubaMessagePage.class);
    }

    /**
     * Retrieves a single message by ID.
     *
     * @param accessToken OAuth2 access token
     * @param mailboxId   mailbox identifier
     * @param messageId   message identifier
     * @return message details
     */
    public ArubaMessageDto getMessage(String accessToken, String mailboxId, String messageId) {
        log.debug("Fetching message {} from mailbox {}", messageId, mailboxId);
        return restClient.get()
                .uri("/mailboxes/{mailboxId}/messages/{messageId}", mailboxId, messageId)
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .retrieve()
                .body(ArubaMessageDto.class);
    }

    /**
     * Submits a new outbound PEC message.
     *
     * @param accessToken OAuth2 access token
     * @param mailboxId   sender mailbox identifier
     * @param request     message payload
     * @return submission acknowledgement
     */
    public ArubaSendMessageResponse sendMessage(
            String accessToken,
            String mailboxId,
            ArubaSendMessageRequest request) {
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
