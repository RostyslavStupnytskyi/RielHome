package com.evolforge.core.auth.service.dto;

import java.time.Instant;

public record TokenPair(String accessToken, Instant accessTokenExpiresAt, String refreshToken) {

    public long expiresInSeconds() {
        return Math.max(0, accessTokenExpiresAt.getEpochSecond() - Instant.now().getEpochSecond());
    }
}
