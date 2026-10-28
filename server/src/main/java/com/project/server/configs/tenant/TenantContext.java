package com.project.server.configs.tenant;

/**
 * Holds the current request's resolved tenant id. Set by {@link TenantResolutionFilter} for real
 * HTTP traffic (or manually, for code paths that run outside a servlet request — tests, the
 * generation cron). Always clear it in a finally block: this is a ThreadLocal, and application
 * servers reuse threads across requests.
 */
public final class TenantContext {
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext(){}

    public static void set(Long tenantId){
        CURRENT_TENANT.set(tenantId);
    }

    public static Long get(){
        return CURRENT_TENANT.get();
    }

    public static void clear(){
        CURRENT_TENANT.remove();
    }
}
