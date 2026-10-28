package com.project.server.db.services.scheduleCombination;

import com.project.server.db.models.ScheduleCombination;
import com.project.server.db.repositories.ScheduleCombinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleCombinationServiceImpl implements ScheduleCombinationService{
    private final ScheduleCombinationRepository repository;

    @Override
    public boolean isExistingCombination(String day, int startTime, int endTime, String className, String timetableCode) {
        return repository.findByCombination(day, startTime, endTime, className, timetableCode).isPresent();
    }

    @Override
    public void save(ScheduleCombination combination) {
        repository.save(combination);
    }

    @Override
    public void save(List<ScheduleCombination> combinations) {
        repository.saveAll(combinations);
    }

    @Override
    public List<ScheduleCombination> getBySchedule(String code) {
        return repository.findBySchedule(code);
    }

    @Override
    public void deleteAllBySchedule(String scheduleCode) {
        repository.deleteAllBySchedule(scheduleCode);
    }

    @Override
    public List<ScheduleCombination> getByFilterParams(String userId, String day, String timetableCode) {
        return repository.findByFilterParams(userId, day, timetableCode);
    }

    @Override
    public Page<ScheduleCombination> getSchedules(String timetableCode, Pageable pageable) {
        return repository.getSchedules(timetableCode, pageable);
    }

    @Override
    public boolean hasScheduleForCourse(String courseCode) {
        return !repository.hasScheduleForCourse(courseCode).isEmpty();
    }
}
