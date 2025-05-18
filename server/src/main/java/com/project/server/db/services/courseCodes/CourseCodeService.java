package com.project.server.db.services.courseCodes;

import com.project.server.db.models.Course;
import com.project.server.db.models.CourseCode;
import com.project.server.exceptions.NotFoundException;

import java.util.List;
import java.util.Optional;

public interface CourseCodeService {
    void createCodes(List<CourseCode> codes);
    void save(CourseCode code);
    boolean isExistingCourseCode(String code);
    void deleteAllByCourseCode(String code);
    List<CourseCode> getCourseCodesByCourse(Course course);
}
