package com.evolforge.core.tenancy.service;

import com.evolforge.core.auth.domain.UserAccount;
import com.evolforge.core.auth.service.MembershipLookup;
import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import com.evolforge.core.tenancy.domain.Membership;
import com.evolforge.core.tenancy.domain.MembershipRole;
import com.evolforge.core.tenancy.domain.Tenant;
import com.evolforge.core.tenancy.repository.MembershipRepository;
import com.evolforge.core.tenancy.repository.TenantRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService implements MembershipLookup {

    private final TenantRepository tenantRepository;
    private final MembershipRepository membershipRepository;

    public TenantService(TenantRepository tenantRepository, MembershipRepository membershipRepository) {
        this.tenantRepository = tenantRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public Tenant createTenantForOwner(UserAccount owner, String tenantName) {
        Tenant tenant = new Tenant();
        tenant.setName(tenantName);
        tenant = tenantRepository.save(tenant);

        Membership membership = new Membership();
        membership.setUser(owner);
        membership.setTenant(tenant);
        membership.setRole(MembershipRole.OWNER);
        membershipRepository.save(membership);

        return tenant;
    }

    @Override
    @Transactional
    public List<MembershipDescriptor> membershipsForUser(UUID userId) {
        return membershipRepository.findByUserId(userId).stream()
                .map(membership -> new MembershipDescriptor(membership.getTenant().getId(),
                        membership.getRole().name()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Membership assignMembership(Tenant tenant, UserAccount user, MembershipRole role) {
        Objects.requireNonNull(tenant, "tenant must not be null");
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(tenant.getId(), "tenant must be persisted before assigning memberships");
        Objects.requireNonNull(user.getId(), "user must be persisted before assigning memberships");

        Membership membership = membershipRepository
                .findByUserIdAndTenantId(user.getId(), tenant.getId())
                .orElseGet(Membership::new);
        membership.setTenant(tenant);
        membership.setUser(user);
        membership.setRole(role);
        return membershipRepository.save(membership);
    }

    @Transactional
    public Membership assignMembership(UUID tenantId, UserAccount user, MembershipRole role) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return assignMembership(tenant, user, role);
    }

    @Transactional
    public Membership updateMembershipRole(UUID tenantId, UUID userId, MembershipRole role) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(role, "role must not be null");

        Membership membership = membershipRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Membership not found for user " + userId + " in tenant " + tenantId));
        membership.setRole(role);
        return membershipRepository.save(membership);
    }

    @Transactional(readOnly = true)
    public List<Membership> listMembers(UUID tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        return membershipRepository.findByTenantId(tenantId);
    }

    @Transactional
    public boolean removeMembership(UUID tenantId, UUID userId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        return membershipRepository.deleteByTenantIdAndUserId(tenantId, userId) > 0;
    }
}
