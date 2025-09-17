package com.evolforge.core.auth.service;

import com.evolforge.core.auth.config.AuthProperties;
import com.evolforge.core.auth.domain.UserAccount;
import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final AuthProperties properties;
    private final ObjectMapper objectMapper;
    private final TokenGenerator tokenGenerator;

    private byte[] secretKey;

    public JwtService(AuthProperties properties, ObjectMapper objectMapper, TokenGenerator tokenGenerator) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.tokenGenerator = tokenGenerator;
    }

    @PostConstruct
    void init() {
        this.secretKey = properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
    }

    public JwtToken generate(UserAccount user, List<MembershipDescriptor> memberships) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getJwt().getAccessTokenTtl());

        Map<String, Object> payload = new HashMap<>();
        payload.put("iss", properties.getJwt().getIssuer());
        payload.put("sub", user.getId().toString());
        payload.put("email", user.getEmail());
        payload.put("name", user.getDisplayName());
        payload.put("iat", issuedAt.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());
        payload.put("jti", tokenGenerator.generateToken(16));
        payload.put("tenants", memberships.stream()
                .map(membership -> Map.of("id", membership.tenantId().toString(), "role", membership.role()))
                .toList());

        String headerJson = toJson(Map.of("alg", "HS512", "typ", "JWT"));
        String payloadJson = toJson(payload);
        String header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String body = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = base64UrlEncode(sign(header + "." + body));
        return new JwtToken(header + "." + body + "." + signature, expiresAt);
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialise JWT payload", e);
        }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA512"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to sign JWT", e);
        }
    }

    private String base64UrlEncode(byte[] value) {
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public record JwtToken(String token, Instant expiresAt) {
    }
}
