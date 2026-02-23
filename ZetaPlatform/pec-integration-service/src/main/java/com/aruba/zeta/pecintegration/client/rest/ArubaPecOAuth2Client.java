package com.aruba.zeta.pecintegration.client.rest;

import java.net.URI;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.aruba.zeta.pecintegration.config.ArubaPecOAuth2Properties;
import com.aruba.zeta.pecintegration.dto.ArubaTokenResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Low-level OAuth2 client for the Aruba authorization server.
 * Handles authorization code exchange and token refreshing.
 */
@Slf4j
@Component
public class ArubaPecOAuth2Client {

    private final RestClient restClient;
    private final ArubaPecOAuth2Properties props;

    public ArubaPecOAuth2Client(
            @Qualifier("arubaOAuth2RestClient") RestClient restClient,
            ArubaPecOAuth2Properties props) {
        this.restClient = restClient;
        this.props = props;
    }

    /**
     * Builds the Aruba authorization URL for user consent.
     *
     * @param state opaque CSRF token
     * @return the authorization URI
     */
    public URI buildAuthorizationUri(String state) {
        URI uri = UriComponentsBuilder.fromUriString(props.getAuthorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", props.getClientId())
                .queryParam("redirect_uri", props.getRedirectUri())
                .queryParam("scope", props.getScope())
                .queryParam("state", state)
                .build()
                .toUri();
        log.debug("Built Aruba authorization URI: {}", uri);
        return uri;
    }

    /**
     * Exchanges the authorization code for an OAuth2 token pair.
     *
     * @param code the authorization code
     * @return token response
     */
    public ArubaTokenResponse exchangeAuthorizationCode(String code) {
        log.debug("Exchanging Aruba authorization code for tokens");
        return restClient.post()
                .body(authCodeForm(code))
                .retrieve()
                .body(ArubaTokenResponse.class);
    }

    /**
     * Refreshes the OAuth2 access token using a refresh token.
     *
     * @param refreshToken the refresh token
     * @return fresh token response
     */
    public ArubaTokenResponse refreshAccessToken(String refreshToken) {
        log.debug("Refreshing Aruba access token");
        return restClient.post()
                .body(refreshTokenForm(refreshToken))
                .retrieve()
                .body(ArubaTokenResponse.class);
    }

    // Request form builders

    private MultiValueMap<String, String> authCodeForm(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", props.getRedirectUri());
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        return form;
    }

    private MultiValueMap<String, String> refreshTokenForm(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());
        return form;
    }
}
