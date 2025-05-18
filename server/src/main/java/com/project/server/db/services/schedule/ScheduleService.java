package com.project.server.db.services.schedule;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.Schedule;
import com.project.server.exceptions.NotFoundException;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface ScheduleService {
    Schedule save(Schedule schedule);
    Schedule getByCode(String code) throws NotFoundException;
    List<Schedule> getByCourseRepresentative(AppUser courseRepresentative);
    Schedule getByCourse( String courseTitle) throws NotFoundException;
    List<Schedule> getByLecturer(AppUser lecturer);
    void deleteByCode(String code);
}