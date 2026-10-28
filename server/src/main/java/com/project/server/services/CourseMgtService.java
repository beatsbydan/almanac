package com.project.server.services;

import com.project.server.db.models.*;
import com.project.server.db.services.level.LevelService;
import com.project.server.db.services.orgUnit.OrgUnitService;
import com.project.server.db.services.schedulableUnit.SchedulableUnitService;
import com.project.server.dtos.request.course.CourseParticipantRequest;
import com.project.server.dtos.request.course.CourseParticipantResponse;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.dtos.response.CourseResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.enums.Type;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * "Course" here is a SchedulableUnit; "course participant" is a SchedulableUnitParticipant whose
 * orgUnits are department-level OrgUnits — generalized per the Blueprint, kept behind this
 * unchanged request/response contract.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CourseMgtService {
    private final SchedulableUnitService schedulableUnitService;
    private final LevelService levelService;
    private final OrgUnitService orgUnitService;
    private final RedissonLockService redissonLockService;

    /**
     * Same unprotected check-then-create shape as college/department/schedule creation and user
     * registration — isExistingByTitle/isExistingCode-then-save() with no lock, only on the
     * create path (an update identifies its course by code already, so it isn't racing a
     * not-yet-committed duplicate). Locks on the title and every course code being claimed.
     */
    public MessageResponse upsertCourse(CourseRequest request, Type type, String code) throws NotFoundException, DuplicateEntryException {
        boolean isCreate = type == Type.CREATE;
        List<String> lockKeys = new ArrayList<>();
        if (isCreate) {
            lockKeys.add("course-registration:title:" + normalizeKey(request.getCourseTitle()));
            for (String courseCode : request.getCourseCodes()) {
                lockKeys.add("course-registration:code:" + normalizeKey(courseCode));
            }
        }
        List<String> locks = acquireLocks(lockKeys.toArray(new String[0]));

        try {
            if (isCreate && locks.size() < lockKeys.size()) throw new DuplicateEntryException("This course is currently being created, please try again.");
            if(schedulableUnitService.isExistingByTitle(request.getCourseTitle()) && isCreate) throw new DuplicateEntryException("This course already exists.");

            SchedulableUnit course;
            if(isCreate){
                course = schedulableUnitService.save(toCreateDbModel(request));
            }
            else{
                course = toUpdateDbModel(schedulableUnitService.getByCode(code), request);
            }

            List<SchedulableUnitParticipant> courseParticipants = toCourseParticipants(request, course);
            List<SchedulableUnitCode> courseCodes = toCourseCodes(request, course, isCreate);

            for (SchedulableUnitParticipant cp : course.getParticipants()) {
                cp.setSchedulableUnit(null);
            }
            course.getParticipants().clear();

            for (SchedulableUnitCode cc : course.getCodes()) {
                cc.setSchedulableUnit(null);
            }
            course.getCodes().clear();

            course.getParticipants().addAll(courseParticipants);
            course.getCodes().addAll(courseCodes);

            schedulableUnitService.save(course);

            String action = isCreate ? "created" : "updated";
            return new MessageResponse(String.format("Course was %s successfully.", action));
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

    public List<CourseResponse> getCourses(
            String unit,
            String department,
            String level,
            boolean shared,
            boolean batched,
            boolean general
    ) {
        Optional<Level> levelOpt = levelService.getByLevelOpt(level);
        Level levelFilter = levelOpt.orElse(null);

        List<SchedulableUnit> courses = noFilterParams(unit, department, level, shared, batched, general) ?
                schedulableUnitService.getAllGeneral()
                :
                schedulableUnitService.getByFilterParams(
                        unit != null ? Integer.parseInt(unit) : 2,
                        null,
                        levelFilter != null ? levelFilter.getCode() : null,
                        shared, batched, general);

        return toResponse(courses);
    }

    public List<CourseResponse> filterCourses(String key) {
        return toResponse(schedulableUnitService.filterByKey(key));
    }

    private boolean noFilterParams(
            String unit,
            String department,
            String level,
            boolean shared,
            boolean batched,
            boolean general
    ) {
        return (unit == null && department == null && level == null && !shared && !batched && !general);
    }

    private List<OrgUnit> getAllDepartments(List<String> departments) throws NotFoundException {
        List<OrgUnit> departmentList = new ArrayList<>();
        if (departments == null) return departmentList;

        for(String departmentName : departments){
            departmentList.add(orgUnitService.getChildByName(departmentName));
        }

        return departmentList;
    }

    private SchedulableUnit toCreateDbModel(CourseRequest request) {
        String size = Arrays.stream(request.getExpectedClassSize().split("< ")).toList().getLast();

        return SchedulableUnit.builder()
                .title(request.getCourseTitle())
                .unit(Integer.valueOf(request.getCourseUnit()))
                .batched(request.isBatched())
                .shared(request.isShared())
                .general(request.isGeneral())
                .expectedClassSize(Integer.valueOf(size))
                .build();
    }

    private List<SchedulableUnitCode> toCourseCodes(CourseRequest request, SchedulableUnit course, boolean isCreate) throws DuplicateEntryException {
        List<SchedulableUnitCode> codes = new ArrayList<>();

        for (String code : request.getCourseCodes()) {
            if(schedulableUnitService.isExistingCode(code) && isCreate) throw new DuplicateEntryException("Course Code already exists");
            codes.add(SchedulableUnitCode.builder().unitCode(code).schedulableUnit(course).build());
        }

        return codes;
    }

    private List<SchedulableUnitParticipant> toCourseParticipants(CourseRequest request, SchedulableUnit course) throws NotFoundException {
        List<SchedulableUnitParticipant> courseParticipants = new ArrayList<>();

        for(CourseParticipantRequest courseParticipantRequest : request.getCourseParticipants()){
            List<OrgUnit> departments = getAllDepartments(courseParticipantRequest.getDepartments());
            Level level = levelService.getByLevel(courseParticipantRequest.getLevel());

            courseParticipants.add(SchedulableUnitParticipant.builder()
                    .schedulableUnit(course)
                    .orgUnits(departments)
                    .level(level)
                    .build());
        }

        return courseParticipants;
    }

    private SchedulableUnit toUpdateDbModel(SchedulableUnit course, CourseRequest request) {
        String size = Arrays.stream(request.getExpectedClassSize().split("< ")).toList().getLast();

        course.setTitle(request.getCourseTitle());
        course.setUnit(Integer.valueOf(request.getCourseUnit()));
        course.setBatched(request.isBatched());
        course.setShared(request.isShared());
        course.setGeneral(request.isGeneral());
        course.setExpectedClassSize(Integer.valueOf(size));

        return course;
    }

    private List<CourseParticipantResponse> toCourseParticipantsResponse(SchedulableUnit course) {
        List<CourseParticipantResponse> response = new ArrayList<>();

        List<SchedulableUnitParticipant> participants = schedulableUnitService.getParticipants(course.getCode());

        for(SchedulableUnitParticipant participant : participants){
            response.add(CourseParticipantResponse.builder()
                            .level(participant.getLevel().getLevel())
                            .departments(participant.getOrgUnits().stream().map(OrgUnit::getName).toList())
                    .build());
        }

        return response;
    }

    private List<CourseResponse> toResponse(List<SchedulableUnit> courses) {
        List<CourseResponse> responses = new ArrayList<>();

        for (SchedulableUnit course : courses) {
            responses.add(
                    CourseResponse.builder()
                            .code(course.getCode())
                            .courseTitle(course.getTitle())
                            .courseUnit(course.getUnit().toString())
                            .batched(course.isBatched())
                            .shared(course.isShared())
                            .general(course.isGeneral())
                            .expectedClassSize(course.getExpectedClassSize().toString())
                            .courseCodes(toCourseCodeResponse(course.getCodes()))
                            .courseParticipants(toCourseParticipantsResponse(course))
                            .build()
            );
        }
        return responses;
    }

    private List<String> toCourseCodeResponse(List<SchedulableUnitCode> codes){
        List<String> response = new ArrayList<>();

        for(SchedulableUnitCode code : codes){
            response.add(code.getUnitCode());
        }

        return response;
    }
}
