package com.aruba.zeta.pecintegration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Deserialised response from the Aruba OAuth2 token endpoint.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArubaTokenResponse {

    /** The bearer token used to authenticate API requests. */
    @JsonProperty("access_token")
    private String accessToken;

    /** Long-lived token used to obtain a new access token when it expires. */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /** Always {@code Bearer} for the Aruba PEC API. */
    @JsonProperty("token_type")
    private String tokenType;

    /** Lifetime of {@code access_token} in seconds from the moment of issuance. */
    @JsonProperty("expires_in")
    private long expiresIn;

    /** Space-separated list of scopes granted by the user. */
    @JsonProperty("scope")
    private String scope;
}
