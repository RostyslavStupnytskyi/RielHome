package com.evolforge.core.tenancy.context;

import com.evolforge.core.auth.service.dto.MembershipDescriptor;
import java.util.List;

/**
 * Marker interface for security principals that carry tenant memberships.
 */
public interface TenantAwarePrincipal {

    List<MembershipDescriptor> memberships();
}
