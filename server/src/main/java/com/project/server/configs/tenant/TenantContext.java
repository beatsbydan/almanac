package com.project.server.configs.tenant;

import org.slf4j.MDC;

/**
 * Holds the current request's resolved tenant id. Set by {@link TenantResolutionFilter} for real
 * HTTP traffic (or manually, for code paths that run outside a servlet request — tests, the
 * generation cron). Always clear it in a finally block: this is a ThreadLocal, and application
 * servers reuse threads across requests.
 *
 * Also mirrors the tenant id into SLF4J's MDC under {@link #MDC_KEY}, so every log line written
 * while a tenant is active can be attributed to it (see logging.pattern.console) — without this, a
 * support engineer reading logs from a multi-tenant deployment has no way to tell which tenant's
 * request produced a given line.
 */
public final class TenantContext {
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();
    private static final String MDC_KEY = "tenantId";

    private TenantContext(){}

    public static void set(Long tenantId){
        CURRENT_TENANT.set(tenantId);
        MDC.put(MDC_KEY, String.valueOf(tenantId));
    }

    public static Long get(){
        return CURRENT_TENANT.get();
    }

    public static void clear(){
        CURRENT_TENANT.remove();
        MDC.remove(MDC_KEY);
    }
}
