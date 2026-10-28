package com.project.server.db.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Generalizes CourseParticipant — which OrgUnit(s) (and, where relevant, which Level) a
 * SchedulableUnit is being taught to. A university course taken by two departments at level 200
 * is {orgUnits: [deptA, deptB], level: 200}; a K-12 subject taught to one class is
 * {orgUnits: [thatClass], level: null} — Level is intentionally optional here, since a school's
 * Class/Arm org unit already encodes what stage the students are at without a separate concept.
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "schedulable_unit_participants")
@NoArgsConstructor
@AllArgsConstructor
public class SchedulableUnitParticipant extends TenantOwnedModel {
    @OneToOne
    @JoinColumn(name = "level_id", referencedColumnName = "id")
    private Level level;

    @ManyToMany
    @JoinTable(
            name = "schedulable_unit_participant_org_units",
            joinColumns = @JoinColumn(name = "participant_id"),
            inverseJoinColumns = @JoinColumn(name = "org_unit_id")
    )
    private List<OrgUnit> orgUnits;

    @ManyToOne
    @JoinColumn(name = "schedulable_unit_id", referencedColumnName = "id")
    private SchedulableUnit schedulableUnit;
}
