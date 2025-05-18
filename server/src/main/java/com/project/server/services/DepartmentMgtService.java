package com.project.server.services;

import com.project.server.db.models.AppUser;
import com.project.server.db.models.College;
import com.project.server.db.models.Department;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.college.CollegeService;
import com.project.server.db.services.department.DepartmentService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentMgtService {
    private final DepartmentService departmentService;
    private final CollegeService collegeService;
    private final AppUserService userService;

    public MessageResponse createDepartment(DepartmentRequest request) throws DuplicateEntryException, NotFoundException {
        if(departmentService.isExistingDepartment(request)) throw new DuplicateEntryException("Department already exists");

        College college = null;
        if(request.getCollege() != null && !request.getCollege().isEmpty()) college = collegeService.getByName(request.getCollege());

        departmentService.save(toCreateDbModel(college, request));
        return new MessageResponse("Department created successfully.");
    }

    public MessageResponse updateDepartment(String code, DepartmentRequest request) throws NotFoundException {
        Department department = departmentService.getByCode(code);

        College college = null;
        if(request.getCollege() != null && !request.getCollege().isEmpty()) college = collegeService.getByName(request.getCollege());

        departmentService.save(toUpdateDbModel(department, college, request));

        return new MessageResponse("Department updated successfully.");
    }

    public List<DepartmentResponse> getDepartments(String college) throws NotFoundException {
        if(!college.isEmpty()) return toResponse(departmentService.getAllByCollege(college));
        else {
            String userId = HelperUtil.getCurrentUserName();
            Optional<AppUser> user = userService.getByUserIdOpt(userId);

            return user.map(appUser -> toResponse(departmentService.getAllByCollege(appUser.getDepartment().getCollege().getShortName()))).orElseGet(() -> toResponse(new ArrayList<>()));
        }
    }

    public List<DepartmentResponse> getByDepartmentName(String name){
        List<Department> departments = departmentService.getAllByMatch(name);
        return toResponse(departments);
    }

    private Department toCreateDbModel(College college, DepartmentRequest request){
        return Department.builder()
                .name(request.getDepartmentName())
                .shortName(request.getDepartmentShortName())
                .college(college)
                .build();
    }

    private Department toUpdateDbModel(Department department, College college, DepartmentRequest request){
        department.setName(request.getDepartmentName());
        department.setShortName(request.getDepartmentShortName());
        department.setCollege(college);

        return department;
    }

    private List<DepartmentResponse> toResponse(List<Department> departments){
        List<DepartmentResponse> responses = new ArrayList<>();
        for(Department department : departments){
            responses.add(DepartmentResponse.builder()
                            .code(department.getCode())
                            .college(department.getCollege().getShortName())
                    .departmentName(department.getName())
                    .departmentShortName(department.getShortName())
                    .build());
        }
        return responses;
    }
}
