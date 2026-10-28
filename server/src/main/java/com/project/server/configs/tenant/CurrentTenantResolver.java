package com.project.server.configs.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Tells Hibernate which tenant the current session belongs to, so every entity extending
 * TenantOwnedModel gets filtered/stamped by it automatically. Plain Hibernate SPI, not a Spring
 * bean — it's instantiated once and wired in via TenantHibernateConfig's
 * HibernatePropertiesCustomizer, since Spring Boot's autoconfiguration doesn't pick this up on
 * its own the way it does for schema/database-per-tenant multi-tenancy.
 */
public class CurrentTenantResolver implements CurrentTenantIdentifierResolver<Long> {

    /**
     * Spring/Hibernate open sessions outside any request at least once — during repository bean
     * wiring at startup — when TenantContext can't possibly be set yet. Falling back to the
     * seeded default tenant's id (always 1: the first and only row inserted by
     * tenant_schema.xml's seed changeset before anything else touches that table) keeps that
     * bootstrap session resolvable instead of hard-failing startup. Request-scoped code should
     * never rely on this fallback — TenantResolutionFilter/BaseIntegrationTest set TenantContext
     * explicitly for every real request and test.
     */
    private static final Long BOOTSTRAP_TENANT_ID = 1L;

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long tenantId = TenantContext.get();
        return tenantId != null ? tenantId : BOOTSTRAP_TENANT_ID;
    }

    /**
     * false, deliberately: in real usage each HTTP request gets its own fresh Hibernate session
     * with a tenant resolved once at open time and never changed, so this validation wouldn't
     * protect anything real — and strict-mode integration tests that exercise more than one
     * tenant inside a single @Transactional test method (one shared session) would otherwise be
     * rejected for "changing" tenants mid-session, which is a test-harness artifact, not an actual
     * cross-tenant leak.
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
