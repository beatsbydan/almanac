package com.project.server.services;

import com.project.server.db.models.ClassRoom;
import com.project.server.db.models.OrgUnit;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.orgUnit.OrgUnitService;
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
    private final OrgUnitService orgUnitService;

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
        OrgUnit college = null;
        if(request.getCollege() != null) college = orgUnitService.getRootByName(request.getCollege());
        return ClassRoom.builder()
                .name(request.getClassName())
                .capacity(request.getClassSize())
                .orgUnit(college)
                .build();
    }
    private ClassRoom toUpdateDbModel(ClassRoom classRoom, ClassroomRequest request) throws NotFoundException {
        OrgUnit college = null;
        if(request.getCollege() != null) college = orgUnitService.getRootByName(request.getCollege());

        classRoom.setName(request.getClassName());
        classRoom.setCapacity(request.getClassSize());
        classRoom.setOrgUnit(college);

        return classRoom;
    }
    private List<ClassroomResponse> toResponse(List<ClassRoom> classRooms){
        List<ClassroomResponse> responses = new ArrayList<>();

        for(ClassRoom room : classRooms){
            String college = room.getOrgUnit() != null ? room.getOrgUnit().getShortName() : "NO COLLEGE";
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
