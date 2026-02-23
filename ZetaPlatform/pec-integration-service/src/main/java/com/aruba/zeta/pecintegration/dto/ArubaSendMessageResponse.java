package com.aruba.zeta.pecintegration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Response body returned by message sending endpoint.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArubaSendMessageResponse {

    /** Aruba-assigned identifier for the newly created message. */
    @JsonProperty("message_id")
    private String messageId;

    /** Initial status of the message. */
    @JsonProperty("status")
    private String status;

    /** ISO-8601 timestamp when Aruba accepted the message for delivery. */
    @JsonProperty("accepted_at")
    private String acceptedAt;
}
