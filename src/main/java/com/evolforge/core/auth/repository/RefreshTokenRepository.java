package com.evolforge.core.auth.repository;

import com.evolforge.core.auth.domain.UserAccount;
import com.evolforge.core.auth.domain.token.RefreshToken;
import com.evolforge.core.infra.persistence.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends BaseRepository<RefreshToken> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndRevokedIsFalse(UserAccount user);

    void deleteByUserId(UUID userId);
}
