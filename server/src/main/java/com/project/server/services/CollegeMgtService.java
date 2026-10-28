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
    private final RedissonLockService redissonLockService;

    /**
     * isExistingRoot()-then-save() had no lock — same unprotected check-then-create shape as
     * schedule creation and user registration, both found racing under k6 load testing. Locks on
     * both name and shortName, since isExistingRoot matches on either.
     */
    public MessageResponse createCollege(CollegeRequest request) throws DuplicateEntryException{
        List<String> locks = acquireLocks(
                "college-registration:name:" + normalizeKey(request.getCollegeName()),
                "college-registration:shortname:" + normalizeKey(request.getCollegeShortName())
        );
        try {
            if (locks.size() < 2) throw new DuplicateEntryException("This college is currently being created, please try again.");
            if(orgUnitService.isExistingRoot(request.getCollegeName(), request.getCollegeShortName())) throw new DuplicateEntryException("College already exists");

            orgUnitService.save(toCreateDbModel(request));

            return new MessageResponse("College created successfully.");
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
