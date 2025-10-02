package com.evolforge.core.tenancy.context;

import java.util.Optional;
import java.util.UUID;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

/**
 * Helper for storing and retrieving tenant information from the Reactor context.
 */
public final class TenantContextHolder {

    private static final Class<TenantContext> CONTEXT_KEY = TenantContext.class;

    private TenantContextHolder() {
    }

    public static Mono<TenantContext> currentContext() {
        return Mono.deferContextual(contextView -> Mono.justOrEmpty(extractContext(contextView)));
    }

    public static Mono<UUID> currentTenantId() {
        return currentContext().map(TenantContext::tenantId);
    }

    public static Mono<Boolean> hasRole(com.evolforge.core.tenancy.domain.MembershipRole role) {
        return currentContext().map(ctx -> ctx.role() == role).defaultIfEmpty(false);
    }

    public static Context put(Context context, TenantContext tenantContext) {
        return context.put(CONTEXT_KEY, tenantContext);
    }

    public static Context clear(Context context) {
        return context.delete(CONTEXT_KEY);
    }

    private static Optional<TenantContext> extractContext(ContextView contextView) {
        return contextView.getOrEmpty(CONTEXT_KEY).map(TenantContext.class::cast);
    }
}
