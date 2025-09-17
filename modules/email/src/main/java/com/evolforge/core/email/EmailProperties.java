package com.evolforge.core.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    private String from;
    private String verificationBaseUrl;
    private String resetBaseUrl;

    public EmailProperties(@DefaultValue("no-reply@rielhome.local") String from,
            @DefaultValue("http://localhost:8080/verify-email") String verificationBaseUrl,
            @DefaultValue("http://localhost:8080/reset-password") String resetBaseUrl) {
        this.from = from;
        this.verificationBaseUrl = verificationBaseUrl;
        this.resetBaseUrl = resetBaseUrl;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getVerificationBaseUrl() {
        return verificationBaseUrl;
    }

    public void setVerificationBaseUrl(String verificationBaseUrl) {
        this.verificationBaseUrl = verificationBaseUrl;
    }

    public String getResetBaseUrl() {
        return resetBaseUrl;
    }

    public void setResetBaseUrl(String resetBaseUrl) {
        this.resetBaseUrl = resetBaseUrl;
    }

    public String buildVerificationLink(String token) {
        return appendToken(verificationBaseUrl, token);
    }

    public String buildPasswordResetLink(String token) {
        return appendToken(resetBaseUrl, token);
    }

    private String appendToken(String baseUrl, String token) {
        if (!StringUtils.hasText(baseUrl)) {
            return token;
        }
        String separator = baseUrl.contains("?") ? "&" : "?";
        if (baseUrl.endsWith("?") || baseUrl.endsWith("&")) {
            separator = "";
        }
        return baseUrl + separator + "token=" + token;
    }
}
