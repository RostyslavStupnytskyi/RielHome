package com.evolforge.core.auth.repository;

import com.evolforge.core.auth.domain.token.PasswordResetToken;
import com.evolforge.core.infra.persistence.BaseRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends BaseRepository<PasswordResetToken> {

    Optional<PasswordResetToken> findByToken(String token);
}
