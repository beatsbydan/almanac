package com.project.server.db.services.scheduleCombination;

import com.project.server.db.models.ScheduleCombination;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ScheduleCombinationService {
    boolean isExistingCombination(
            String day,
            int startTime,
            int endTime,
            String className,
            String timetableCode);
    void save(ScheduleCombination combination);
    void save(List<ScheduleCombination> combinations);
    List<ScheduleCombination> getBySchedule(String code);
    void deleteAllBySchedule(String scheduleCode);
    List<ScheduleCombination> getByFilterParams(String userId, String day, String timetableCode);
    Page<ScheduleCombination> getSchedules(String timetableCode, Pageable pageable);
    boolean hasScheduleForCourse(String courseCode);
}