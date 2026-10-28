package com.project.server.db.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Generalizes College and Department into one self-referential hierarchy of arbitrary depth.
 * A university's College -> Department becomes two OrgUnit levels; a primary/secondary school's
 * Form -> Stream (or just a flat Class with no parent) is the same shape with different labels.
 *
 * `type` is a free-text, tenant-defined label ("College", "Department", "Class", "Arm", ...)
 * rather than an enum — baking a fixed set of institution-shaped type names into code is exactly
 * the kind of hardcoded assumption this model exists to remove.
 *
 * Introduced alongside College/Department, not replacing them yet — see the migration plan notes
 * in generalized_academic_model.xml for why the cutover is a separate step.
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "org_units")
@NoArgsConstructor
@AllArgsConstructor
public class OrgUnit extends TenantOwnedModel {
    private String name;

    private String shortName;

    private String type;

    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private OrgUnit parent;

    @OneToMany(mappedBy = "parent")
    private List<OrgUnit> children;
}
