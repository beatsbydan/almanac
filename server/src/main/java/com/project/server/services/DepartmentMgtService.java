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
    private final RedissonLockService redissonLockService;

    /**
     * Same unprotected check-then-create shape as college/schedule creation and user
     * registration — isExistingChild()-then-save() with no lock. Locks on both name and
     * shortName, since isExistingChild matches on either.
     */
    public MessageResponse createDepartment(DepartmentRequest request) throws DuplicateEntryException, NotFoundException {
        List<String> locks = acquireLocks(
                "department-registration:name:" + normalizeKey(request.getDepartmentName()),
                "department-registration:shortname:" + normalizeKey(request.getDepartmentShortName())
        );
        try {
            if (locks.size() < 2) throw new DuplicateEntryException("This department is currently being created, please try again.");
            if(orgUnitService.isExistingChild(request.getDepartmentName(), request.getDepartmentShortName())) throw new DuplicateEntryException("Department already exists");

            OrgUnit college = null;
            if(request.getCollege() != null && !request.getCollege().isEmpty()) college = orgUnitService.getRootByName(request.getCollege());

            orgUnitService.save(toCreateDbModel(college, request));
            return new MessageResponse("Department created successfully.");
        } finally {
            releaseLocks(locks);
        }
    }

    private List<String> acquireLocks(String... keys){
        List<String> locks = new ArrayList<>();
        for (String key : keys) {
            if (redissonLockService.tryLock(key, 5, 15)) locks.add(key);
        }
        return locks;
    }

    private void releaseLocks(List<String> locks){
        for (String lock : locks) {
            try {
                redissonLockService.unlock(lock);
            } catch (Exception e) {
                log.warn("Failed to release lock {}: {}", lock, e.getMessage());
            }
        }
    }

    private String normalizeKey(String value){
        return value == null ? "" : value.trim().toUpperCase();
    }

    public MessageResponse updateDepartment(String code, DepartmentRequest request) throws NotFoundException {
        OrgUnit department = orgUnitService.getByCode(code);

        OrgUnit college = null;
        if(request.getCollege() != null && !request.getCollege().isEmpty()) college = orgUnitService.getRootByName(request.getCollege());

        orgUnitService.save(toUpdateDbModel(department, college, request));

        return new MessageResponse("Department updated successfully.");
    }

    /**
     * With no college param, falls back to "departments under the current user's own college" —
     * but an ADMINISTRATOR (seeded with no orgUnit at all, see ServerStartUpMgtService) or any
     * user whose department was created without a college (college is optional on
     * DepartmentRequest) has no college to fall back to. Either is a normal, reachable state, not
     * a data-integrity problem, so this returns an empty list rather than NPEing.
     */
    public List<DepartmentResponse> getDepartments(String college) throws NotFoundException {
        if(!college.isEmpty()) return toResponse(orgUnitService.getAllByParentName(college));
        else {
            String userId = HelperUtil.getCurrentUserName();
            Optional<AppUser> user = userService.getByUserIdOpt(userId);

            return user
                    .map(AppUser::getOrgUnit)
                    .map(OrgUnit::getParent)
                    .map(parent -> toResponse(orgUnitService.getAllByParentName(parent.getShortName())))
                    .orElseGet(() -> toResponse(new ArrayList<>()));
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
