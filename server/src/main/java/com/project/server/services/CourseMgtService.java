package com.project.server.services;

import com.project.server.db.models.*;
import com.project.server.db.services.course.CourseService;
import com.project.server.db.services.courseCode.CourseCodeService;
import com.project.server.db.services.courseParticipant.CourseParticipantService;
import com.project.server.db.services.department.DepartmentService;
import com.project.server.db.services.level.LevelService;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseMgtService {
    private final CourseService courseService;
    private final LevelService levelService;
    private final DepartmentService departmentService;
    private final CourseCodeService courseCodeService;
    private final CourseParticipantService courseParticipantService;

    public MessageResponse upsertCourse(CourseRequest request, Type type, String code) throws NotFoundException, DuplicateEntryException {
        boolean isCreate = type == Type.CREATE;

        if(courseService.isExistingCourse(request) && isCreate) throw new DuplicateEntryException("This course already exists.");

        Course course;
        if(isCreate){
            course = courseService.save(toCreateDbModel(request));
        }
        else{
            course = toUpdateDbModel(courseService.findByCode(code), request);
        }

        List<CourseParticipant> courseParticipants = toCourseParticipants(request, course);
        List<CourseCode> courseCodes = toCourseCodes(request, course, isCreate);

        for (CourseParticipant cp : course.getCourseParticipants()) {
            cp.setCourse(null);
        }
        course.getCourseParticipants().clear();

        for (CourseCode cc : course.getCourseCodes()) {
            cc.setCourse(null);
        }
        course.getCourseCodes().clear();

        course.getCourseParticipants().addAll(courseParticipants);
        course.getCourseCodes().addAll(courseCodes);

        courseService.save(course);

        String action = isCreate ? "created" : "updated";
        return new MessageResponse(String.format("Course was %s successfully.", action));
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

        List<Course> courses = noFilterParams(unit, department, level, shared, batched, general) ?
                courseService.getAllUniversityWideCourses()
                :
                courseService.getByFilterParams(
                        unit != null ? Integer.parseInt(unit) : 2,
                        null,
                        levelFilter != null ? levelFilter.getCode() : null,
                        shared, batched, general);

        return toResponse(courses);
    }

    public List<CourseResponse> filterCourses(String key) {
        return toResponse(courseService.filterCourses(key));
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

    private List<Department> getAllDepartments(List<String> departments) throws NotFoundException {
        List<Department> departmentList = new ArrayList<>();

        for(String departmentName : departments){
            departmentList.add(departmentService.getByName(departmentName));
        }

        return departmentList;
    }

    private Course toCreateDbModel(CourseRequest request) {
        String size = Arrays.stream(request.getExpectedClassSize().split("< ")).toList().getLast();

        return Course.builder()
                .courseTitle(request.getCourseTitle())
                .courseUnit(Integer.valueOf(request.getCourseUnit()))
                .batched(request.isBatched())
                .shared(request.isShared())
                .general(request.isGeneral())
                .expectedClassSize(Integer.valueOf(size))
                .build();
    }

    private List<CourseCode> toCourseCodes(CourseRequest request, Course course, boolean isCreate) throws DuplicateEntryException {
        List<CourseCode> codes = new ArrayList<>();

        for (String code : request.getCourseCodes()) {
            if(courseCodeService.isExistingCourseCode(code) && isCreate) throw new DuplicateEntryException("Course Code already exists");
            codes.add(CourseCode.builder().courseCode(code).course(course).build());
        }

        return codes;
    }

    private List<CourseParticipant> toCourseParticipants(CourseRequest request, Course course) throws NotFoundException {
        List<CourseParticipant> courseParticipants = new ArrayList<>();

        for(CourseParticipantRequest courseParticipantRequest : request.getCourseParticipants()){
            List<Department> departments = getAllDepartments(courseParticipantRequest.getDepartments());
            Level level = levelService.getByLevel(courseParticipantRequest.getLevel());

            courseParticipants.add(CourseParticipant.builder()
                    .course(course)
                    .departments(departments)
                    .level(level)
                    .build());
        }

        return courseParticipants;
    }

    private Course toUpdateDbModel(Course course, CourseRequest request) {
        String size = Arrays.stream(request.getExpectedClassSize().split("< ")).toList().getLast();

        course.setCourseTitle(request.getCourseTitle());
        course.setCourseUnit(Integer.valueOf(request.getCourseUnit()));
        course.setBatched(request.isBatched());
        course.setShared(request.isShared());
        course.setGeneral(request.isGeneral());
        course.setExpectedClassSize(Integer.valueOf(size));

        return course;
    }

    private List<CourseParticipantResponse> toCourseParticipantsResponse(Course course) {
        List<CourseParticipantResponse> response = new ArrayList<>();

        List<CourseParticipant> participants = courseParticipantService.getByCourse(course.getCode());

        for(CourseParticipant participant : participants){
            response.add(CourseParticipantResponse.builder()
                            .level(participant.getLevel().getLevel())
                            .departments(participant.getDepartments().stream().map(Department::getName).toList())
                    .build());
        }

        return response;
    }

    private List<CourseResponse> toResponse(List<Course> courses) {
        List<CourseResponse> responses = new ArrayList<>();

        for (Course course : courses) {
            responses.add(
                    CourseResponse.builder()
                            .code(course.getCode())
                            .courseTitle(course.getCourseTitle())
                            .courseUnit(course.getCourseUnit().toString())
                            .batched(course.isBatched())
                            .shared(course.isShared())
                            .general(course.isGeneral())
                            .expectedClassSize(course.getExpectedClassSize().toString())
                            .courseCodes(toCourseCodeResponse(course.getCourseCodes()))
                            .courseParticipants(toCourseParticipantsResponse(course))
                            .build()
            );
        }
        return responses;
    }

    private List<String> toCourseCodeResponse(List<CourseCode> codes){
        List<String> response = new ArrayList<>();

        for(CourseCode code : codes){
            response.add(code.getCourseCode());
        }

        return response;
    }
}