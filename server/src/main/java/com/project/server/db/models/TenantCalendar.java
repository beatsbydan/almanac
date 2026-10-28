package com.project.server.db.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Per-tenant replacement for the hardcoded Monday-Friday/8am-6pm grid that used to live in
 * VariableUtil, plus the blackout windows (e.g. Bowen's Wednesday chapel slot) that used to be an
 * if-statement in ScheduleMgtService. Deliberately BaseModel, not TenantOwnedModel — this is a
 * one-row-per-tenant settings record reached directly by its tenant FK, not a collection that
 * needs row-level filtering.
 *
 * daysInUse/periodTimes/durations are stored as comma-separated strings rather than proper child
 * tables — adequate for simple ordered config lists that are read whole and never queried into;
 * worth revisiting if calendars grow richer (per-period labels, varying duration by subject, etc).
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tenant_calendars")
@NoArgsConstructor
@AllArgsConstructor
public class TenantCalendar extends BaseModel {
    @OneToOne
    @JoinColumn(name = "tenant_id", referencedColumnName = "id", unique = true)
    private Tenant tenant;

    private String daysInUse;

    private String periodTimes;

    private String durations;

    private Integer maxEndHour;

    @OneToMany(mappedBy = "tenantCalendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlackoutWindow> blackoutWindows;
}
