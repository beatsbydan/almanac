package com.project.server.db.services.courseParticipant;

import com.project.server.db.models.CourseParticipant;

import java.util.List;

public interface CourseParticipantService {
    void save(CourseParticipant participant);

    List<CourseParticipant> getByCourse(String courseCode);

    void deleteAllByCourse(String courseCode);
}
