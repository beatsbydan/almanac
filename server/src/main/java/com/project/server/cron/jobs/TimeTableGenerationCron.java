package com.project.server.cron.jobs;

import com.project.server.cron.services.TimeTableGenerationCronService;
import com.project.server.exceptions.TimeTableGenerationException;
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
            cronService.generate();
        }
        catch(TimeTableGenerationException e){
            log.warn(e.getMessage());
        }
        finally{
            isRunning.set(false);
            log.info("{} job has completed.", className);
        }
    }
}
