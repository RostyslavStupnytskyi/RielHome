package com.evolforge.core.auth.service;

import com.evolforge.core.auth.config.AuthProperties;
import com.evolforge.core.auth.domain.UserAccount;
import com.evolforge.core.auth.exception.AuthException;
import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtService {

    private final AuthProperties properties;
    private final ObjectMapper objectMapper;
    private final TokenGenerator tokenGenerator;
    private final RevokedTokenStore revokedTokenStore;

    private byte[] secretKey;

    public JwtService(AuthProperties properties, ObjectMapper objectMapper, TokenGenerator tokenGenerator,
            RevokedTokenStore revokedTokenStore) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.tokenGenerator = tokenGenerator;
        this.revokedTokenStore = revokedTokenStore;
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

    public AccessTokenDetails parse(String token) {
        if (!StringUtils.hasText(token)) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }

        byte[] signature = base64UrlDecode(parts[2]);
        byte[] expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(signature, expectedSignature)) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }

        Map<String, Object> header = readJson(base64UrlDecode(parts[0]));
        if (!Objects.equals("HS512", header.get("alg"))) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }

        Map<String, Object> payload = readJson(base64UrlDecode(parts[1]));

        String issuer = Objects.toString(payload.get("iss"), null);
        if (!StringUtils.hasText(issuer) || !Objects.equals(issuer, properties.getJwt().getIssuer())) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }

        String subject = Objects.toString(payload.get("sub"), null);
        if (!StringUtils.hasText(subject)) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }

        UUID userId;
        try {
            userId = UUID.fromString(subject);
        } catch (IllegalArgumentException ex) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }

        Object expiresAtValue = payload.get("exp");
        if (!(expiresAtValue instanceof Number expiresAtNumber)) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }
        Instant expiresAt = Instant.ofEpochSecond(expiresAtNumber.longValue());
        if (expiresAt.isBefore(Instant.now())) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }

        Object tokenIdValue = payload.get("jti");
        if (!(tokenIdValue instanceof String tokenId) || !StringUtils.hasText(tokenId)) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }
        if (revokedTokenStore.isRevoked(tokenId)) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }

        List<MembershipDescriptor> memberships = extractMemberships(payload.get("tenants"));
        String email = Objects.toString(payload.get("email"), null);
        String displayName = Objects.toString(payload.get("name"), null);

        return new AccessTokenDetails(userId, email, displayName, expiresAt, memberships, tokenId);
    }

    public void revoke(String tokenId, Instant expiresAt) {
        revokedTokenStore.revoke(tokenId, expiresAt);
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialise JWT payload", e);
        }
    }

    private Map<String, Object> readJson(byte[] json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (IOException e) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
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

    private byte[] base64UrlDecode(String value) {
        try {
            return Base64.getUrlDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw AuthException.unauthorized("auth.access_invalid", "Access token is invalid or expired");
        }
    }

    private List<MembershipDescriptor> extractMemberships(Object value) {
        if (!(value instanceof List<?> rawList)) {
            return List.of();
        }

        List<MembershipDescriptor> memberships = new ArrayList<>();
        for (Object element : rawList) {
            if (element instanceof Map<?, ?> entry) {
                Object idValue = entry.get("id");
                Object roleValue = entry.get("role");
                if (idValue instanceof String idString && StringUtils.hasText(idString)
                        && roleValue instanceof String roleString) {
                    try {
                        memberships.add(new MembershipDescriptor(UUID.fromString(idString), roleString));
                    } catch (IllegalArgumentException ignored) {
                        // skip invalid entries
                    }
                }
            }
        }
        return memberships;
    }

    public record JwtToken(String token, Instant expiresAt) {
    }

    public record AccessTokenDetails(UUID userId, String email, String displayName, Instant expiresAt,
            List<MembershipDescriptor> memberships, String tokenId) {
    }
}
