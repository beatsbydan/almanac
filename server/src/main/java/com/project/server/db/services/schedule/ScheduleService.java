package com.project.server.db.services.schedule;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.CourseParticipant;
import com.project.server.db.models.Schedule;
import com.project.server.exceptions.NotFoundException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ScheduleService {
    Schedule save(Schedule schedule);
    void saveAll(List<Schedule> schedule);
    Schedule getByCode(String code) throws NotFoundException;
    List<Schedule> getByCourseRepresentative(AppUser courseRepresentative);
    Schedule getByCourse( String courseTitle) throws NotFoundException;
    List<Schedule> getByLecturer(AppUser lecturer);
    Page<Schedule> getUnprocessedUniversityWideSchedules(Pageable pageable);
    Page<Schedule> getUnprocessedSchedulesByCollege(String college, Pageable pageable);
    void deleteByCode(String code);
    boolean hasScheduleAtTime(int startTime, int endTime, List<CourseParticipant> participants, String day);
}