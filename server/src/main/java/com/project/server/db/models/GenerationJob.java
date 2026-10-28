package com.project.server.db.models;

import com.project.server.enums.GenerationJobStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Tracks one asynchronous run of {@code GenerationOrchestrationService.generate()} so the trigger
 * endpoint can return immediately instead of holding an HTTP request (and a DB transaction) open
 * for however long the external solver takes. See GenerationWorker for the actual async execution
 * and status transitions (PENDING -> RUNNING -> SOLVED/FAILED).
 *
 * infeasibleUnitCodes/skippedNoTeacherUnitCodes are stored comma-separated rather than as child
 * tables — same convention as TenantCalendar's daysInUse/periodTimes, adequate for short lists
 * that are only ever read back whole, never queried into.
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "generation_jobs")
@NoArgsConstructor
@AllArgsConstructor
public class GenerationJob extends TenantOwnedModel {
    @Enumerated(EnumType.STRING)
    private GenerationJobStatus status;

    private Integer scheduledCount;

    private String message;

    private String infeasibleUnitCodes;

    private String skippedNoTeacherUnitCodes;
}
