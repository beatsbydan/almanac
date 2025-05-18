package com.project.server.db.services.courseParticipant;

import com.project.server.db.models.CourseParticipant;
import com.project.server.db.repositories.CourseParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseParticipantServiceImpl implements CourseParticipantService{
    private final CourseParticipantRepository repository;

    @Override
    public void save(CourseParticipant participant) {
        repository.save(participant);
    }

    @Override
    public List<CourseParticipant> getByCourse(String courseCode) {
        return repository.findByCourse(courseCode);
    }

    @Override
    public void deleteAllByCourse(String courseCode) {
        repository.deleteAllByCourse(courseCode);
    }
}
