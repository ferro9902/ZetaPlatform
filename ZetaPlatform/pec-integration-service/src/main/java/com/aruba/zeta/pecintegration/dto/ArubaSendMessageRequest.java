package com.aruba.zeta.pecintegration.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * Request body for mailboxes and messages endpoints.
 *
 * <p>Attachments are identified by their document ID in Aruba's storage layer;
 * the API resolves and attaches the files server-side.
 */
@Data
@Builder
public class ArubaSendMessageRequest {

    /** Recipient PEC address . */
    @JsonProperty("to")
    private String to;

    /** Message subject. */
    @JsonProperty("subject")
    private String subject;

    /** Plain-text body of the PEC message. */
    @JsonProperty("body")
    private String body;

    /** Optional list of Aruba document IDs to attach. */
    @JsonProperty("document_ids")
    private List<String> documentIds;
}
