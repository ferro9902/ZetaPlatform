package com.aruba.zeta.pecintegration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for Aruba PEC OAuth2 integration.
 * Binds to {@code aruba.oauth2.*}.
 */
@Data
@ConfigurationProperties(prefix = "aruba.oauth2")
public class ArubaPecOAuth2Properties {

    private String clientId;

    private String clientSecret;

    private String authorizationUri;

    private String tokenUri;

    private String redirectUri;

    private String scope;

    private String apiBaseUrl;

    private int connectTimeoutMs = 5_000;

    private int readTimeoutMs = 30_000;
}
