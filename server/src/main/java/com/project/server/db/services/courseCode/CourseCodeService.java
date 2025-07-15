package com.project.server.db.services.courseCode;

import com.project.server.db.models.Course;
import com.project.server.db.models.CourseCode;

import java.util.List;

public interface CourseCodeService {
    void createCodes(List<CourseCode> codes);
    void save(CourseCode code);
    boolean isExistingCourseCode(String code);
    void deleteAllByCourseCode(String code);
    List<CourseCode> getCourseCodesByCourse(Course course);
}
