package com.evolforge.core.tenancy.context;

import com.evolforge.core.tenancy.domain.MembershipRole;
import java.util.UUID;

/**
 * Context describing the current tenant and caller role.
 */
public record TenantContext(UUID tenantId, MembershipRole role) {
}
