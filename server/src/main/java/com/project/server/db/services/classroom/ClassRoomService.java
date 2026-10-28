package com.project.server.db.services.classroom;

import com.project.server.db.models.ClassRoom;
import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.exceptions.NotFoundException;

import java.util.List;

public interface ClassRoomService {
    boolean isExistingClassroom(ClassroomRequest request);
    void save(ClassRoom classRoom);
    ClassRoom findByCode(String code) throws NotFoundException;
    List<ClassRoom> findAllByCollege(String college);
    List<ClassRoom> filterByCollege(String college);
    List<ClassRoom> getClassroomsWithoutCollege();
    List<ClassRoom> filterByNameOrCollege(String key);
    List<ClassRoom> getAll();
}
