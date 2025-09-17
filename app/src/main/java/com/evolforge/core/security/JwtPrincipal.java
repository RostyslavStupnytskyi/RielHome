package com.evolforge.core.security;

import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import com.evolforge.core.tenancy.context.TenantAwarePrincipal;
import java.util.List;
import java.util.UUID;

public record JwtPrincipal(UUID userId, String email, String displayName, List<MembershipDescriptor> memberships,
        String tokenId) implements TenantAwarePrincipal {
}
