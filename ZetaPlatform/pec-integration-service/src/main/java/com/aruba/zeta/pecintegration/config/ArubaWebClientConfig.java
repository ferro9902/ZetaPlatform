package com.aruba.zeta.pecintegration.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configures RestClient instances for Aruba OAuth2 and PEC API communication.
 * Applies timeouts from configuration properties.
 */
@Configuration
class ArubaWebClientConfig {

    @Bean("arubaOAuth2RestClient")
    RestClient arubaOAuth2RestClient(ArubaPecOAuth2Properties props) {
        return RestClient.builder()
                .baseUrl(props.getTokenUri())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .requestFactory(buildRequestFactory(props))
                .build();
    }

    @Bean("arubaPecApiRestClient")
    RestClient arubaPecApiRestClient(ArubaPecOAuth2Properties props) {
        return RestClient.builder()
                .baseUrl(props.getApiBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(buildRequestFactory(props))
                .build();
    }

    private SimpleClientHttpRequestFactory buildRequestFactory(ArubaPecOAuth2Properties props) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(props.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(props.getReadTimeoutMs()));
        return factory;
    }
}
