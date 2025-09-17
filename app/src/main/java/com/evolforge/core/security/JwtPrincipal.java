package com.evolforge.core.security;

import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import java.util.List;
import java.util.UUID;

public record JwtPrincipal(UUID userId, String email, String displayName, List<MembershipDescriptor> memberships,
        String tokenId) {
}
