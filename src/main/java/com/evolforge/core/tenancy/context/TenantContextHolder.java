package com.evolforge.core.tenancy.context;

import com.evolforge.core.tenancy.domain.MembershipRole;
import java.util.Optional;
import java.util.UUID;

/**
 * Helper for storing and retrieving tenant information for the current request thread.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static Optional<TenantContext> currentContext() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static Optional<UUID> currentTenantId() {
        return currentContext().map(TenantContext::tenantId);
    }

    public static boolean hasRole(MembershipRole role) {
        return currentContext().map(ctx -> ctx.role() == role).orElse(false);
    }

    public static void set(TenantContext tenantContext) {
        if (tenantContext == null) {
            clear();
        } else {
            CONTEXT.set(tenantContext);
        }
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
