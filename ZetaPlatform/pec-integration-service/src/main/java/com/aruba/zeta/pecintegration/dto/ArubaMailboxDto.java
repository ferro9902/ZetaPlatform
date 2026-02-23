package com.aruba.zeta.pecintegration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Represents a single PEC mailbox as returned by the Aruba PEC REST API.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArubaMailboxDto {

    /** Aruba-assigned mailbox identifier. */
    @JsonProperty("id")
    private String id;

    /** The certified email address. */
    @JsonProperty("pec_address")
    private String pecAddress;

    /** Mailbox lifecycle status as reported by Aruba. */
    @JsonProperty("status")
    private String status;

    /** Human-readable label for the mailbox. */
    @JsonProperty("display_name")
    private String displayName;

    /** ISO-8601 timestamp of when the mailbox was provisioned in Aruba's system. */
    @JsonProperty("created_at")
    private String createdAt;
}
