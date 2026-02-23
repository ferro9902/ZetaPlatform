package com.aruba.zeta.pecintegration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Metadata for an attachment carried by an {@link ArubaMessageDto}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArubaAttachmentDto {

    /** Aruba-assigned attachment identifier. */
    @JsonProperty("id")
    private String id;

    /** Original filename of the attachment (e.g. {@code fattura.pdf}). */
    @JsonProperty("file_name")
    private String fileName;

    /** MIME type (e.g. {@code application/pdf}). */
    @JsonProperty("content_type")
    private String contentType;

    /** File size in bytes. */
    @JsonProperty("size")
    private long size;
}
