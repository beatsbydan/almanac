package com.project.server.db.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Generalizes Course into "whatever gets placed on a timetable against a teacher, a room, and a
 * day/time" — a university course taken by students across departments, or a K-12 subject taught
 * to one class, are the same shape: a title, a set of participants (SchedulableUnitParticipant),
 * and the constraints a schedule for it has to satisfy (expected size, whether it's shared across
 * multiple participant groups, etc).
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "schedulable_units")
@NoArgsConstructor
@AllArgsConstructor
public class SchedulableUnit extends TenantOwnedModel {
    private String title;

    private Integer unit;

    private boolean batched;

    private boolean shared;

    private boolean general;

    private Integer expectedClassSize;

    @Builder.Default
    @OneToMany(mappedBy = "schedulableUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SchedulableUnitCode> codes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "schedulableUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SchedulableUnitParticipant> participants = new ArrayList<>();
}
