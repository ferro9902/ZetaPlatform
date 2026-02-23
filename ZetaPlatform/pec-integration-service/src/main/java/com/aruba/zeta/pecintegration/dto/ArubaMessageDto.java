package com.aruba.zeta.pecintegration.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Represents a single PEC message as returned by the Aruba PEC REST API.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArubaMessageDto {

    /** Aruba-assigned unique message identifier. */
    @JsonProperty("id")
    private String id;

    /** PEC address of the sender. */
    @JsonProperty("from")
    private String from;

    /** PEC address of the primary recipient. */
    @JsonProperty("to")
    private String to;

    /** Message subject line. */
    @JsonProperty("subject")
    private String subject;

    /** Plain-text body of the PEC message. */
    @JsonProperty("body")
    private String body;

    /** Delivery status as reported by Aruba. */
    @JsonProperty("status")
    private String status;

    /** ISO-8601 timestamp of the official send/receive time. */
    @JsonProperty("received_at")
    private String receivedAt;

    /** Attachments carried by this message (may be empty). */
    @JsonProperty("attachments")
    private List<ArubaAttachmentDto> attachments;
}
