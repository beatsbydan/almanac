package com.project.server.services;

import com.project.server.db.models.OrgUnit;
import com.project.server.db.services.orgUnit.OrgUnitService;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.dtos.response.CollegeResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * "College" here is an OrgUnit with no parent (a root org unit) — generalized per the Blueprint's
 * OrgUnit model, kept behind this unchanged request/response contract so nothing calling this
 * service (controller, tests) had to change.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeMgtService {
    private final OrgUnitService orgUnitService;

    public MessageResponse createCollege(CollegeRequest request) throws DuplicateEntryException{
        if(orgUnitService.isExistingRoot(request.getCollegeName(), request.getCollegeShortName())) throw new DuplicateEntryException("College already exists");

        orgUnitService.save(toCreateDbModel(request));

        return new MessageResponse("College created successfully.");
    }

    public MessageResponse updateCollege(String code, CollegeRequest request) throws NotFoundException {
        OrgUnit college = orgUnitService.getByCode(code);

        orgUnitService.save(toUpdateDbModel(college, request));

        return new MessageResponse("College created successfully.");
    }

    public List<CollegeResponse> getColleges(){
        return toResponse(orgUnitService.getRoots());
    }

    private OrgUnit toCreateDbModel(CollegeRequest request){
        return OrgUnit.builder()
                .name(request.getCollegeName())
                .shortName(request.getCollegeShortName())
                .type("College")
                .build();
    }

    private OrgUnit toUpdateDbModel(OrgUnit college, CollegeRequest request){
        college.setName(request.getCollegeName());
        college.setShortName(request.getCollegeShortName());

        return college;
    }

    private List<CollegeResponse> toResponse(List<OrgUnit> colleges){
        List<CollegeResponse> responses = new ArrayList<>();
        for(OrgUnit college : colleges){
            responses.add(CollegeResponse.builder()
                            .code(college.getCode())
                    .collegeShortName(college.getShortName())
                    .collegeName(college.getName())
                    .build());
        }
        return responses;
    }
}
