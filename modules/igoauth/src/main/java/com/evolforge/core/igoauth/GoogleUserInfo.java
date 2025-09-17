package com.evolforge.core.igoauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfo(
        String sub,
        String email,
        @JsonProperty("email_verified") boolean emailVerified,
        @JsonProperty("name") String name) {
}
