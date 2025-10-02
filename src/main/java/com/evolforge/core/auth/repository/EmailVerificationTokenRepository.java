package com.evolforge.core.auth.repository;

import com.evolforge.core.auth.domain.token.EmailVerificationToken;
import com.evolforge.core.infra.persistence.BaseRepository;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends BaseRepository<EmailVerificationToken> {

    Optional<EmailVerificationToken> findByToken(String token);
}
