package com.project.server.db.models;

import com.project.server.enums.InstitutionType;
import com.project.server.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

/**
 * The platform's tenant registry. Deliberately NOT a {@link TenantOwnedModel} — this is the one
 * table every tenant-scoped row points back to, so it must stay outside Hibernate's per-tenant
 * row filtering rather than being filtered by it.
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tenants")
@NoArgsConstructor
@AllArgsConstructor
public class Tenant extends BaseModel {
    private String name;

    private String slug;

    @Enumerated(EnumType.STRING)
    private InstitutionType institutionType;

    @Enumerated(EnumType.STRING)
    private Status status;
}
