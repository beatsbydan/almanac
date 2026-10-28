package com.project.server.services;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.ClassRoom;
import com.project.server.db.models.OrgUnit;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.orgUnit.OrgUnitService;
import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.dtos.response.ClassroomResponse;
import com.project.server.dtos.response.MessageResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassroomMgtServiceTest extends BaseIntegrationTest {
    @Autowired
    private ClassroomMgtService classroomMgtService;
    @Autowired
    private ClassRoomService classRoomService;
    @Autowired
    private OrgUnitService orgUnitService;
    private MessageResponse messageResponse;

    @BeforeEach
    @SneakyThrows
    void init(){
        orgUnitService.save(OrgUnit.builder().name(toClassRoomRequest().getCollege()).shortName("DEPT").type("College").build());
        messageResponse = classroomMgtService.createClassroom(toClassRoomRequest());
    }

    @Test
    @SneakyThrows
    void should_create_classroom() {
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_update_classroom() {
        ClassRoom room = classRoomService.findAllByCollege("college").getFirst();
        messageResponse = classroomMgtService.updateClassroom(room.getCode(), toClassRoomRequest());

        assertNotNull(messageResponse);
    }

    /**
     * The college here is seeded with name="college", shortName="DEPT" (deliberately different,
     * see init()) — filterByCollege used to match only shortName, so looking a classroom up by
     * its college's full name (the same identifier format accepted when creating it) silently
     * returned nothing. assertNotNull alone doesn't catch that, since an empty list is also
     * non-null — found by actually exercising this through the live API, not just this test.
     */
    @Test
    @SneakyThrows
    void should_find_classrooms() {
        List<ClassroomResponse> responses = classroomMgtService.findClassroomsByCollege("college");

        assertEquals(1, responses.size());
    }

    @Test
    @SneakyThrows
    void should_filter_classrooms_by_college_or_name() {
        List<ClassroomResponse> responses = classroomMgtService.filterClassrooms("college");

        assertNotNull(responses);
    }
    private ClassroomRequest toClassRoomRequest(){
        return ClassroomRequest.builder()
                .className("classname")
                .classSize(200)
                .college("college")
                .build();
    }
}