package com.aruba.zeta.pecintegration.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Paginated wrapper returned by mailboxes and messages endpoints.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArubaMessagePage {

    @JsonProperty("messages")
    private List<ArubaMessageDto> messages;

    /** 0-based page index of this result set. */
    @JsonProperty("page")
    private int page;

    /** Number of items per page requested. */
    @JsonProperty("size")
    private int size;

    /** Total number of messages available across all pages. */
    @JsonProperty("total_elements")
    private long totalElements;

    /** Total number of pages available for the requested page size. */
    @JsonProperty("total_pages")
    private int totalPages;
}
