package com.evolforge.core.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    @NotNull
    private Jwt jwt = new Jwt();

    @NotNull
    private RefreshToken refreshToken = new RefreshToken();

    @NotNull
    private Verification verification = new Verification();

    @NotNull
    private PasswordReset passwordReset = new PasswordReset();

    @NotNull
    private Password password = new Password();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Verification getVerification() {
        return verification;
    }

    public void setVerification(Verification verification) {
        this.verification = verification;
    }

    public PasswordReset getPasswordReset() {
        return passwordReset;
    }

    public void setPasswordReset(PasswordReset passwordReset) {
        this.passwordReset = passwordReset;
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    public static class Jwt {

        @NotBlank
        private String secret = "change-me-change-me-change-me-change-me-change-me-change-me-change";

        private Duration accessTokenTtl = Duration.ofMinutes(15);

        @NotBlank
        private String issuer = "rielhome";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }

    public static class RefreshToken {

        private Duration ttl = Duration.ofDays(30);

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }

    public static class Verification {

        private Duration ttl = Duration.ofHours(24);

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }

    public static class PasswordReset {

        private Duration ttl = Duration.ofHours(1);

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }

    public static class Password {

        private String encoder = "argon2";

        public Password() {
        }

        public Password(@DefaultValue("argon2") String encoder) {
            this.encoder = encoder;
        }

        public String getEncoder() {
            return encoder;
        }

        public void setEncoder(String encoder) {
            this.encoder = encoder;
        }
    }
}
