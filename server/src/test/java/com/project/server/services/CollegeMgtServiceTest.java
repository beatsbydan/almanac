package com.project.server.services;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.OrgUnit;
import com.project.server.db.services.orgUnit.OrgUnitService;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.dtos.response.CollegeResponse;
import com.project.server.dtos.response.MessageResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollegeMgtServiceTest extends BaseIntegrationTest {
    @Autowired
    private CollegeMgtService mgtService;

    @Autowired
    private OrgUnitService collegeService;

    private MessageResponse messageResponse;

    @BeforeEach
    @SneakyThrows
    void init(){
        messageResponse = mgtService.createCollege(toCollegeRequest());
    }

    @Test
    @SneakyThrows
    void should_create_college() {
        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_update_college() {
        OrgUnit college = collegeService.getRootByName(toCollegeRequest().getCollegeName());
        messageResponse = mgtService.updateCollege(college.getCode() , toCollegeRequest());

        assertNotNull(messageResponse);
    }

    @Test
    @SneakyThrows
    void should_get_colleges() {
        List<CollegeResponse> response = mgtService.getColleges();

        assertNotNull(response);
    }

    private CollegeRequest toCollegeRequest(){
        return CollegeRequest.builder()
                .collegeName("collegeName")
                .collegeShortName("collegeShortName")
                .build();
    }

}