package com.project.server.db.services.course;

import com.project.server.db.models.Course;
import com.project.server.db.repositories.CourseRepository;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService{
    private final CourseRepository repository;

    @Override
    public Course findByCode(String code) throws NotFoundException {
        return repository.findByCode(code).orElseThrow(()-> new NotFoundException("This course does not exist."));
    }

    @Override
    public Course findByTitle(String title) throws NotFoundException {
        return repository.findByCourseTitle(title).orElseThrow(()-> new NotFoundException("This course does not exist."));
    }

    @Override
    public Course findByCourseCode(String courseCode) throws NotFoundException {
        return repository.findByCourseCode(courseCode).orElseThrow(()-> new NotFoundException("Course not found."));
    }

    @Override
    public boolean isExistingCourse(CourseRequest request) {
        return repository.findByCourseTitle(request.getCourseTitle()).isPresent();
    }

    @Override
    public Course save(Course course) {
        return repository.save(course);
    }

    @Override
    public List<Course> getAllUniversityWideCourses() {
        return repository.findAllUniversityWideCourses();
    }

    @Override
    public List<Course> getByFilterParams(
            int unit,
            String department,
            String levelCode,
            boolean shared,
            boolean batched,
            boolean general
    ) {
        return repository.findAllByFilterParams(unit, department, levelCode, shared, batched, general);
    }

    @Override
    public List<Course> filterCourses(String key) {
        return repository.filterCourses(key);
    }
}
