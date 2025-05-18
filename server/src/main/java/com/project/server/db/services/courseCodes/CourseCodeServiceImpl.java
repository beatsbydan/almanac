package com.project.server.db.services.courseCodes;

import com.project.server.db.models.Course;
import com.project.server.db.models.CourseCode;
import com.project.server.db.repositories.CourseCodeRepository;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseCodeServiceImpl implements CourseCodeService{
    private final CourseCodeRepository repository;

    @Override
    public void createCodes(List<CourseCode> codes) {
        repository.saveAll(codes);
    }

    @Override
    public void save(CourseCode code) {
        repository.save(code);
    }

    @Override
    public boolean isExistingCourseCode(String code) {
        return repository.findByCourseCode(code).isPresent();
    }

    @Override
    public void deleteAllByCourseCode(String courseCode) {
        repository.deleteAllByCourseCode(courseCode);
    }

    @Override
    public List<CourseCode> getCourseCodesByCourse(Course course) {
        return repository.findByCourse(course.getCode());
    }
}
