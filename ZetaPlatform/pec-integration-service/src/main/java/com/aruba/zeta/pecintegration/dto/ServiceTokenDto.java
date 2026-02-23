package com.aruba.zeta.pecintegration.dto;

import lombok.Data;

/**
 * Domain representation of a stored OAuth2 service token,
 * decoupled from the gRPC transport types.
 */
@Data
public class ServiceTokenDto {

    /** Whether a token was found for the requested user/service combination. */
    private boolean found;

    /** OAuth2 access token (plaintext, decrypted by user-auth-service). */
    private String accessToken;

    /** OAuth2 refresh token used to obtain a new access token when expired. */
    private String refreshToken;

    /** Access token expiration timestamp in epoch seconds. */
    private long expiresAt;
}
