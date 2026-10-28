package com.project.server.db.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

/** Generalizes CourseCode — one SchedulableUnit can have multiple codes (cross-listed courses, shared subjects). */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "schedulable_unit_codes")
@NoArgsConstructor
@AllArgsConstructor
public class SchedulableUnitCode extends TenantOwnedModel {
    private String unitCode;

    @ManyToOne
    @JoinColumn(name = "schedulable_unit_id", referencedColumnName = "id")
    private SchedulableUnit schedulableUnit;
}
