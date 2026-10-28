package com.project.server.services;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.OrgUnit;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.orgUnit.OrgUnitService;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.dtos.response.DepartmentResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import com.project.server.util.HelperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** "Department" here is an OrgUnit with a parent (a root-level "college" org unit). */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentMgtService {
    private final OrgUnitService orgUnitService;
    private final AppUserService userService;

    public MessageResponse createDepartment(DepartmentRequest request) throws DuplicateEntryException, NotFoundException {
        if(orgUnitService.isExistingChild(request.getDepartmentName(), request.getDepartmentShortName())) throw new DuplicateEntryException("Department already exists");

        OrgUnit college = null;
        if(request.getCollege() != null && !request.getCollege().isEmpty()) college = orgUnitService.getRootByName(request.getCollege());

        orgUnitService.save(toCreateDbModel(college, request));
        return new MessageResponse("Department created successfully.");
    }

    public MessageResponse updateDepartment(String code, DepartmentRequest request) throws NotFoundException {
        OrgUnit department = orgUnitService.getByCode(code);

        OrgUnit college = null;
        if(request.getCollege() != null && !request.getCollege().isEmpty()) college = orgUnitService.getRootByName(request.getCollege());

        orgUnitService.save(toUpdateDbModel(department, college, request));

        return new MessageResponse("Department updated successfully.");
    }

    public List<DepartmentResponse> getDepartments(String college) throws NotFoundException {
        if(!college.isEmpty()) return toResponse(orgUnitService.getAllByParentName(college));
        else {
            String userId = HelperUtil.getCurrentUserName();
            Optional<AppUser> user = userService.getByUserIdOpt(userId);

            return user.map(appUser -> toResponse(orgUnitService.getAllByParentName(appUser.getOrgUnit().getParent().getShortName()))).orElseGet(() -> toResponse(new ArrayList<>()));
        }
    }

    public List<DepartmentResponse> getByDepartmentName(String name){
        List<OrgUnit> departments = orgUnitService.getAllByMatch(name);
        return toResponse(departments);
    }

    private OrgUnit toCreateDbModel(OrgUnit college, DepartmentRequest request){
        return OrgUnit.builder()
                .name(request.getDepartmentName())
                .shortName(request.getDepartmentShortName())
                .type("Department")
                .parent(college)
                .build();
    }

    private OrgUnit toUpdateDbModel(OrgUnit department, OrgUnit college, DepartmentRequest request){
        department.setName(request.getDepartmentName());
        department.setShortName(request.getDepartmentShortName());
        department.setParent(college);

        return department;
    }

    private List<DepartmentResponse> toResponse(List<OrgUnit> departments){
        List<DepartmentResponse> responses = new ArrayList<>();
        for(OrgUnit department : departments){
            String collegeShortName = department.getParent() != null ? department.getParent().getShortName() : null;
            responses.add(DepartmentResponse.builder()
                            .code(department.getCode())
                            .college(collegeShortName)
                    .departmentName(department.getName())
                    .departmentShortName(department.getShortName())
                    .build());
        }
        return responses;
    }
}
