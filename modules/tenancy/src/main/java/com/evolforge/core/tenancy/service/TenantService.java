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
}
