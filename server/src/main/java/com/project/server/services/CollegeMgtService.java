package com.project.server.services;

import com.project.server.db.models.College;
import com.project.server.db.services.college.CollegeService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeMgtService {
    private final CollegeService collegeService;

    public MessageResponse createCollege(CollegeRequest request) throws DuplicateEntryException{
        if(collegeService.isExistingCollege(request)) throw new DuplicateEntryException("College already exists");

        collegeService.save(toCreateDbModel(request));

        return new MessageResponse("College created successfully.");
    }

    public MessageResponse updateCollege(String code, CollegeRequest request) throws NotFoundException {
        College college = collegeService.getByCode(code);

        collegeService.save(toUpdateDbModel(college, request));

        return new MessageResponse("College created successfully.");
    }

    public List<CollegeResponse> getColleges(){
        return toResponse(collegeService.getAllColleges());
    }

    private College toCreateDbModel(CollegeRequest request){
        return College.builder()
                .name(request.getCollegeName())
                .shortName(request.getCollegeShortName())
                .build();
    }

    private College toUpdateDbModel(College college, CollegeRequest request){
        college.setName(request.getCollegeName());
        college.setShortName(request.getCollegeShortName());

        return college;
    }

    private List<CollegeResponse> toResponse(List<College> colleges){
        List<CollegeResponse> responses = new ArrayList<>();
        for(College college : colleges){
            responses.add(CollegeResponse.builder()
                            .code(college.getCode())
                    .collegeShortName(college.getShortName())
                    .collegeName(college.getName())
                    .build());
        }
        return responses;
    }
}
