package com.project.server.db.services.course;

import com.project.server.db.models.Course;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.exceptions.NotFoundException;

import java.util.List;

public interface CourseService {
    Course findByCode(String code) throws NotFoundException;
    Course findByTitle(String title) throws NotFoundException;
    Course save(Course course);
    boolean isExistingCourse(CourseRequest request);
    List<Course> getAllUniversityWideCourses();
    List<Course> getByFilterParams(
            int unit,
            String department,
            String levelCode,
            boolean shared,
            boolean batched,
            boolean general
    );

    List<Course> filterCourses(String key);
}
