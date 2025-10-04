package com.evolforge.core.igoauth;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleOAuthService {

    private final GoogleOAuthProperties properties;
    private final RestClient restClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public GoogleOAuthService(GoogleOAuthProperties properties, RestClient googleOAuthRestClient) {
        this.properties = properties;
        this.restClient = googleOAuthRestClient;
    }

    public String buildAuthorizationUrl(String stateHint) {
        ensureConfigured();
        String state = StringUtils.hasText(stateHint) ? stateHint : randomState();
        return UriComponentsBuilder.fromHttpUrl(properties.getAuthorizationUri())
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", properties.getScope())
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    public GoogleOAuthResult exchangeCode(String code) {
        ensureConfigured();
        Objects.requireNonNull(code, "code");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("redirect_uri", properties.getRedirectUri());
        form.add("grant_type", "authorization_code");

        GoogleTokenResponse tokenResponse = restClient.post()
                .uri(properties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);

        if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
            throw new IllegalStateException("Failed to exchange Google OAuth code");
        }

        GoogleUserInfo userInfo = restClient.get()
                .uri(properties.getUserInfoUri())
                .headers(headers -> headers.setBearerAuth(tokenResponse.accessToken()))
                .retrieve()
                .body(GoogleUserInfo.class);

        if (userInfo == null) {
            throw new IllegalStateException("Failed to load Google user profile");
        }

        GoogleProfile profile = new GoogleProfile(userInfo.sub(), userInfo.email(), userInfo.emailVerified(), userInfo.name());
        return new GoogleOAuthResult(profile, tokenResponse);
    }

    private void ensureConfigured() {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("Google OAuth is not configured");
        }
    }

    private String randomState() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
