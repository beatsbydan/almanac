package com.project.server.services;

import com.project.server.db.models.ClassRoom;
import com.project.server.db.models.College;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.college.CollegeService;
import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.dtos.response.ClassroomResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomMgtService {
    private final ClassRoomService classRoomService;
    private final CollegeService collegeService;

    public MessageResponse createClassroom (ClassroomRequest request) throws DuplicateEntryException, NotFoundException {
        if(classRoomService.isExistingClassroom(request)) throw new DuplicateEntryException("Classroom already exists.");

        classRoomService.save(toCreateDbModel(request));

        return new MessageResponse("Classroom created successfully.");
    }
    public MessageResponse updateClassroom (String code, ClassroomRequest request) throws NotFoundException {
        ClassRoom classRoom = classRoomService.findByCode(code);

        classRoomService.save(toUpdateDbModel(classRoom, request));

        return new MessageResponse("Classroom updated successfully.");
    }
    public List<ClassroomResponse> findClassroomsByCollege(String college){
        return college == null ? toResponse(classRoomService.getClassroomsWithoutCollege()) : toResponse(classRoomService.filterByCollege(college));
    }
    public List<ClassroomResponse> filterClassrooms(String key){
        return toResponse(classRoomService.filterByNameOrCollege(key));
    }

    private ClassRoom toCreateDbModel(ClassroomRequest request) throws NotFoundException {
        College college = null;
        if(request.getCollege() != null) college = collegeService.getByName(request.getCollege());
        return ClassRoom.builder()
                .name(request.getClassName())
                .capacity(request.getClassSize())
                .college(college)
                .build();
    }
    private ClassRoom toUpdateDbModel(ClassRoom classRoom, ClassroomRequest request) throws NotFoundException {
        College college = null;
        if(request.getCollege() != null) college = collegeService.getByName(request.getCollege());

        classRoom.setName(request.getClassName());
        classRoom.setCapacity(request.getClassSize());
        classRoom.setCollege(college);

        return classRoom;
    }
    private List<ClassroomResponse> toResponse(List<ClassRoom> classRooms){
        List<ClassroomResponse> responses = new ArrayList<>();

        for(ClassRoom room : classRooms){
            String college = room.getCollege() != null ? room.getCollege().getShortName() : "NO COLLEGE";
            responses.add(ClassroomResponse.builder()
                            .className(room.getName())
                            .code(room.getCode())
                            .classSize(room.getCapacity())
                            .college(college)
                    .build());
        }

        return responses;
    }
}
