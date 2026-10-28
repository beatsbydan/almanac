package com.project.server.services.generation;

import com.project.server.configs.tenant.TenantContext;
import com.project.server.db.models.GenerationJob;
import com.project.server.db.services.generationJob.GenerationJobService;
import com.project.server.dtos.request.generation.GenerationTriggerRequest;
import com.project.server.dtos.response.generation.GenerationResultResponse;
import com.project.server.enums.GenerationJobStatus;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Runs a generation job off the request thread. run() is the real entry point used by
 * GenerationOrchestrationService.trigger() — it owns setting/clearing TenantContext, since that's
 * a ThreadLocal and @Async dispatches onto a different thread than the one the HTTP request (and
 * TenantResolutionFilter) ran on. runSync() is the actual logic, kept separate and package-visible
 * so tests can exercise the RUNNING/SOLVED/FAILED transitions directly and synchronously instead
 * of racing a background thread.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenerationWorker {
    private final GenerationOrchestrationService generationOrchestrationService;
    private final GenerationJobService generationJobService;

    @Async
    public void run(String jobCode, Long tenantId, GenerationTriggerRequest request) {
        TenantContext.set(tenantId);
        try {
            runSync(jobCode, request);
        } finally {
            TenantContext.clear();
        }
    }

    void runSync(String jobCode, GenerationTriggerRequest request) {
        try {
            GenerationJob job = generationJobService.getByCode(jobCode);
            job.setStatus(GenerationJobStatus.RUNNING);
            generationJobService.save(job);

            GenerationResultResponse result = generationOrchestrationService.generate(request);

            job.setStatus(GenerationJobStatus.SOLVED);
            job.setScheduledCount(result.getScheduledCount());
            job.setMessage(result.getMessage());
            job.setInfeasibleUnitCodes(String.join(",", result.getInfeasibleUnitCodes()));
            job.setSkippedNoTeacherUnitCodes(String.join(",", result.getSkippedNoTeacherUnitCodes()));
            generationJobService.save(job);
        } catch (Exception e) {
            log.error("Generation job {} failed: {}", jobCode, e.getMessage(), e);
            recordFailure(jobCode, e.getMessage());
        }
    }

    private void recordFailure(String jobCode, String message) {
        try {
            GenerationJob job = generationJobService.getByCode(jobCode);
            job.setStatus(GenerationJobStatus.FAILED);
            job.setMessage(message);
            generationJobService.save(job);
        } catch (NotFoundException e) {
            log.error("Could not record failure for generation job {}: job not found.", jobCode);
        }
    }
}
