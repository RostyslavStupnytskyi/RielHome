package com.evolforge.core.auth.repository;

import com.evolforge.core.auth.domain.UserAccount;
import com.evolforge.core.infra.persistence.BaseRepository;
import java.util.Optional;

public interface UserAccountRepository extends BaseRepository<UserAccount> {

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByGoogleSub(String googleSub);
}
