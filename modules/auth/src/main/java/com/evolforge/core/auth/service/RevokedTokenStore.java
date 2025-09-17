package com.evolforge.core.auth.service;

import java.time.Instant;

public interface RevokedTokenStore {

    void revoke(String tokenId, Instant expiresAt);

    boolean isRevoked(String tokenId);
}
