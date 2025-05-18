package com.project.server.db.services.schedule;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.Schedule;
import com.project.server.db.repositories.ScheduleRepository;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{
    private final ScheduleRepository repository;

    @Override
    public Schedule save(Schedule schedule) {
        return repository.save(schedule);
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
    public void deleteByCode(String code) {
        repository.deleteByCode(code);
    }
}