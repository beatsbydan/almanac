package com.project.server.db.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.TenantId;

/**
 * Base class for every entity that belongs to a single tenant. The {@link TenantId} field is
 * populated and filtered automatically by Hibernate from whatever
 * {@code CurrentTenantIdentifierResolver} is configured (see configs/tenant) — no repository or
 * service method needs to add a tenant check itself; Hibernate adds it to every query, insert,
 * update, and delete against entities that extend this class.
 */
@MappedSuperclass
public abstract class TenantOwnedModel extends BaseModel {
    @Getter
    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;
}
