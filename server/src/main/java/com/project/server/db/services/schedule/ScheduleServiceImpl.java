package com.project.server.db.services.schedule;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.CourseParticipant;
import com.project.server.db.models.Schedule;
import com.project.server.db.repositories.ScheduleRepository;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService{
    private final ScheduleRepository repository;

    @Override
    public Schedule save(Schedule schedule) {
        return repository.save(schedule);
    }

    @Override
    public void saveAll(List<Schedule> schedules) {
        repository.saveAll(schedules);
    }

    @Override
    public Schedule getByCode(String code) throws NotFoundException {
        return repository.findByCode(code);
    }

    @Override
    public List<Schedule> getByCourseRepresentative(AppUser courseRepresentative) {
        return repository.findByCourseRepresentative(
                courseRepresentative.getLevel().getLevel(),
                courseRepresentative.getDepartment().getName()
        );
    }

    @Override
    public Schedule getByCourse(String courseTitle) throws NotFoundException {
        return repository.findByCourse(courseTitle);
    }

    @Override
    public List<Schedule> getByLecturer(AppUser lecturer) {
        return repository.findByLecturer(lecturer.getUserId());
    }

    @Override
    public Page<Schedule> getUnprocessedUniversityWideSchedules(Pageable pageable) {
        return getUnprocessedUniversityWideSchedulesWithDetails(pageable);
    }

    @Override
    public Page<Schedule> getUnprocessedSchedulesByCollege(String college, Pageable pageable) {
        return getUnprocessedSchedulesWithDetailsByCollege(college, pageable);
    }

    @Override
    public void deleteByCode(String code) {
        repository.deleteByCode(code);
    }

    @Override
    public boolean hasScheduleAtTime(int startTime, int endTime, List<CourseParticipant> participants, String day) {
        log.info("Checking startTime: {}, endTime: {}, day: {}", startTime, endTime, day);
        List<Long> participantIds = participants.stream().map(CourseParticipant::getId).toList();

        List<Schedule> schedules = repository.findConflictingSchedules(startTime, endTime, participantIds, day);
        log.info("found:{} schedules", schedules.size());
        return !schedules.isEmpty();
    }

    public Page<Schedule> getUnprocessedSchedulesWithDetailsByCollege(String college, Pageable pageable) {
        Page<Long> scheduleIdsPage = repository.findUnprocessedScheduleIdsByCollege(college, pageable);

        List<Schedule> schedules;

        if (scheduleIdsPage.isEmpty()) {
            schedules = new ArrayList<>();
        }
        else {
            schedules = repository.fetchUnprocessedSchedulesWithDetailsByCollegeByIds(scheduleIdsPage.getContent());
        }

        return new PageImpl<>(schedules, pageable, scheduleIdsPage.getTotalElements());
    }

    public Page<Schedule> getUnprocessedUniversityWideSchedulesWithDetails(Pageable pageable) {
        Page<Long> scheduleIdsPage = repository.findUnprocessedUniversityWideScheduleIds(pageable);

        List<Schedule> schedules;

        if (scheduleIdsPage.isEmpty()) {
            schedules = new ArrayList<>();
        }
        else {
            schedules = repository.fetchUnprocessedUniversityWideSchedulesByIds(scheduleIdsPage.getContent());
        }

        return new PageImpl<>(schedules, pageable, scheduleIdsPage.getTotalElements());
    }
}