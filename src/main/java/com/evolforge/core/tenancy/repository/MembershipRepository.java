package com.evolforge.core.tenancy.repository;

import com.evolforge.core.tenancy.domain.Membership;
import com.evolforge.core.infra.persistence.BaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends BaseRepository<Membership> {

    List<Membership> findByUserId(UUID userId);

    List<Membership> findByTenantId(UUID tenantId);

    Optional<Membership> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    long deleteByTenantIdAndUserId(UUID tenantId, UUID userId);
}
