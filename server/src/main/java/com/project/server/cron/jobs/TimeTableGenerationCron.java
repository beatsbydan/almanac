package com.project.server.cron.jobs;

import com.project.server.configs.tenant.TenantContext;
import com.project.server.cron.services.TimeTableGenerationCronService;
import com.project.server.db.services.tenant.TenantService;
import com.project.server.exceptions.TimeTableGenerationException;
import com.project.server.util.VariableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTableGenerationCron {
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final TimeTableGenerationCronService cronService;
    private final TenantService tenantService;

    /**
     * The cron runs outside any HTTP request, so there's no TenantResolutionFilter to set
     * TenantContext for it. Until generation is reworked to loop over every tenant (Blueprint
     * Phase 2/3), it still only serves the single legacy tenant — scoped explicitly here rather
     * than left unset, which would make every tenant-owned query fail outright.
     */
    @Scheduled(cron = "${project.generation.cron.exp:* */2 * * * *}")
    @SchedulerLock(
            name = "TimeTable_Generation_Cron",
            lockAtLeastFor = "${project.cron.min.lock:PT5M}",
            lockAtMostFor = "${project.cron.max.lock:PT10M}"
    )
    public void start(){
        isRunning.set(true);
        String className = this.getClass().getSimpleName();

        if(isRunning.get()){
            log.info("{} job is running.", className);
        }
        try{
            TenantContext.set(tenantService.getBySlug(VariableUtil.DEFAULT_TENANT_SLUG).getId());
            cronService.generate();
        }
        catch(TimeTableGenerationException e){
            log.warn(e.getMessage());
        }
        catch(Exception e){
            log.warn("Could not resolve the default tenant for generation: {}", e.getMessage());
        }
        finally{
            TenantContext.clear();
            isRunning.set(false);
            log.info("{} job has completed.", className);
        }
    }
}
