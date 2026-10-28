package com.project.server.db.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

/**
 * A recurring non-teaching window on a tenant's calendar (e.g. Bowen's Wednesday 10am-12pm
 * chapel/assembly slot) — what used to be a hardcoded day/time check inside ScheduleMgtService.
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "blackout_windows")
@NoArgsConstructor
@AllArgsConstructor
public class BlackoutWindow extends BaseModel {
    @ManyToOne
    @JoinColumn(name = "tenant_calendar_id", referencedColumnName = "id")
    private TenantCalendar tenantCalendar;

    private String day;

    private Integer startTime;

    private Integer endTime;

    private String label;
}
