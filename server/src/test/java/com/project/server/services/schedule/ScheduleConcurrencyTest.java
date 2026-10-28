package com.project.server.services.schedule;

import com.project.server.BaseIntegrationTest;
import com.project.server.configs.tenant.TenantContext;
import com.project.server.db.models.ClassRoom;
import com.project.server.db.models.SchedulableUnit;
import com.project.server.db.models.Schedule;
import com.project.server.db.repositories.AppUserRepository;
import com.project.server.db.repositories.ClassRoomRepository;
import com.project.server.db.repositories.SchedulableUnitRepository;
import com.project.server.db.services.classroom.ClassRoomService;
import com.project.server.db.services.schedulableUnit.SchedulableUnitService;
import com.project.server.db.services.schedule.ScheduleService;
import com.project.server.db.services.scheduleCombination.ScheduleCombinationService;
import com.project.server.db.services.timetable.TimeTableService;
import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.dtos.request.ScheduleRequest;
import com.project.server.dtos.request.TimetableRequest;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.dtos.request.course.CourseParticipantRequest;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.enums.Type;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.ScheduleUpsertException;
import com.project.server.services.AuthMgtService;
import com.project.server.services.ClassroomMgtService;
import com.project.server.services.CollegeMgtService;
import com.project.server.services.CourseMgtService;
import com.project.server.services.DepartmentMgtService;
import com.project.server.services.timetable.TimeTableMgtService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Isolated in its own class (not bundled into ScheduleMgtServiceTest) because real concurrency
 * requires work to happen on separate threads/connections/transactions — unlike every other test
 * in this codebase, this one has to commit its fixtures mid-test (TestTransaction.end()) rather
 * than rely on @Transactional rollback, so it uses run-tagged unique names throughout and cleans
 * up explicitly, to guarantee it can never collide with or leak into any other test's fixtures.
 */
@Slf4j
class ScheduleConcurrencyTest extends BaseIntegrationTest {
    @Autowired
    private ScheduleMgtService scheduleMgtService;
    @Autowired
    private AuthMgtService authMgtService;
    @Autowired
    private ClassroomMgtService classroomMgtService;
    @Autowired
    private DepartmentMgtService departmentMgtService;
    @Autowired
    private CollegeMgtService collegeMgtService;
    @Autowired
    private CourseMgtService courseMgtService;
    @Autowired
    private TimeTableMgtService timeTableMgtService;
    @Autowired
    private ClassRoomService classRoomService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ScheduleCombinationService scheduleCombinationService;
    @Autowired
    private SchedulableUnitService courseService;
    @Autowired
    private TimeTableService timeTableService;
    @Autowired
    private ClassRoomRepository classRoomRepository;
    @Autowired
    private SchedulableUnitRepository schedulableUnitRepository;
    @Autowired
    private AppUserRepository appUserRepository;

    /**
     * The classroom and participant/department locks in ScheduleMgtService are both keyed by
     * (day, timePeriod, ...) — concurrent requests for the SAME course at DIFFERENT
     * day/time/classroom combinations acquire entirely disjoint locks, so neither blocked the
     * other, and both could read hasScheduleForCourse as false before either committed. Found via
     * k6 load testing (7 of 10 concurrent requests for one course all "succeeded"), fixed by
     * ScheduleMgtService.acquireCourseLock. Each worker here uses its own thread, connection, and
     * transaction — the same shape as concurrent HTTP requests — so (a) the fixtures below have to
     * be actually committed for the workers to see them, and (b) each worker thread has to set
     * TenantContext itself (it's a ThreadLocal tied to the thread that called
     * TenantResolutionFilter/BaseIntegrationTest.setup(), which these worker threads never did).
     */
    @Test
    @SneakyThrows
    void should_allowOnlyOneConcurrentScheduleCreation_forTheSameCourse() {
        String tag = "race" + System.currentTimeMillis();
        int concurrency = 8;
        Long tenantId = TenantContext.get();

        timeTableMgtService.deactivate();
        timeTableMgtService.createTimeTable(toTimeTableRequest(tag));
        timeTableMgtService.changeScheduleCreationStatus(timeTableService.getActiveTimeTable().getCode(), "ENABLED");
        collegeMgtService.createCollege(toCollegeRequest(tag));
        departmentMgtService.createDepartment(toDepartmentRequest(tag));
        authMgtService.createUser(toStaffSignUpRequest(tag));
        for (int i = 0; i < concurrency; i++) {
            classroomMgtService.createClassroom(toClassRoomRequest(tag, i));
        }
        List<String> classroomCodes = classRoomService.findAllByCollege(collegeName(tag)).stream()
                .map(ClassRoom::getCode)
                .toList();
        courseMgtService.upsertCourse(toCourseRequest(tag), Type.CREATE, null);
        SchedulableUnit course = courseService.getByTitle(courseTitle(tag));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        try {
            AtomicInteger successes = new AtomicInteger();
            AtomicInteger conflicts = new AtomicInteger();
            List<Exception> unexpected = new ArrayList<>();

            ExecutorService pool = Executors.newFixedThreadPool(concurrency);
            CountDownLatch ready = new CountDownLatch(concurrency);
            CountDownLatch go = new CountDownLatch(1);

            for (int i = 0; i < concurrency; i++) {
                int idx = i;
                pool.execute(() -> {
                    TenantContext.set(tenantId);
                    try {
                        ready.countDown();
                        try { go.await(); } catch (InterruptedException ignored) {}
                        try {
                            scheduleMgtService.createSchedule(toScheduleRequest(tag, course.getCode(), classroomCodes.get(idx), idx));
                            successes.incrementAndGet();
                        } catch (ScheduleUpsertException | DuplicateEntryException e) {
                            conflicts.incrementAndGet();
                        } catch (Exception e) {
                            synchronized (unexpected) { unexpected.add(e); }
                        }
                    } finally {
                        TenantContext.clear();
                    }
                });
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS), "Workers failed to reach the starting line");
            go.countDown();
            pool.shutdown();
            assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS), "Workers did not finish in time");

            assertTrue(unexpected.isEmpty(), "Unexpected exceptions: " + unexpected);
            assertEquals(1, successes.get(), "Exactly one concurrent create should succeed for one course");
            assertEquals(concurrency - 1, conflicts.get());
        } finally {
            cleanup(tag, course, classroomCodes);
            TenantContext.set(tenantId);
            TestTransaction.start();
        }
    }

    private void cleanup(String tag, SchedulableUnit course, List<String> classroomCodes) {
        try {
            try {
                Schedule schedule = scheduleService.getByCourse(courseTitle(tag));
                scheduleCombinationService.deleteAllBySchedule(schedule.getCode());
                scheduleService.deleteByCode(schedule.getCode());
            } catch (Exception ignored) {
                // no schedule got created — fine, cleanup must not itself fail the test over it
            }
            schedulableUnitRepository.delete(courseService.getByCodeWithParticipants(course.getCode()));
            for (String code : classroomCodes) {
                classRoomRepository.findByCode(code).ifPresent(classRoomRepository::delete);
            }
            appUserRepository.findByUserId(tag.toUpperCase()).ifPresent(appUserRepository::delete);
            timeTableMgtService.deactivate();
        } catch (Exception e) {
            log.warn("Race test cleanup did not fully complete: {}", e.getMessage());
        }
    }

    private String collegeName(String tag){ return "college-" + tag; }
    private String departmentName(String tag){ return "department-" + tag; }
    private String courseTitle(String tag){ return "course-" + tag; }

    private TimetableRequest toTimeTableRequest(String tag){
        return TimetableRequest.builder()
                .session(tag + "/" + tag)
                .semester("FIRST")
                .creationStartDate("2003-03-09")
                .creationEndDate("2003-03-12")
                .build();
    }

    private CollegeRequest toCollegeRequest(String tag){
        return CollegeRequest.builder()
                .collegeName(collegeName(tag))
                .collegeShortName(("CLG" + tag).substring(0, Math.min(15, ("CLG" + tag).length())))
                .build();
    }

    private DepartmentRequest toDepartmentRequest(String tag){
        return DepartmentRequest.builder()
                .departmentName(departmentName(tag))
                .departmentShortName(("DPT" + tag).substring(0, Math.min(15, ("DPT" + tag).length())))
                .college(collegeName(tag))
                .build();
    }

    private StaffSignUpRequest toStaffSignUpRequest(String tag){
        return StaffSignUpRequest.builder()
                .staffEmail(tag + "@load.test")
                .password("password")
                .staffId(tag)
                .build();
    }

    private ClassroomRequest toClassRoomRequest(String tag, int idx){
        return ClassroomRequest.builder()
                .className("room-" + tag + "-" + idx)
                .classSize(200)
                .college(collegeName(tag))
                .build();
    }

    private CourseRequest toCourseRequest(String tag){
        List<CourseParticipantRequest> participantRequests = new ArrayList<>();
        participantRequests.add(CourseParticipantRequest.builder().level("100").departments(List.of(departmentName(tag))).build());
        return CourseRequest.builder()
                .courseTitle(courseTitle(tag))
                .courseUnit("0")
                .general(false)
                .shared(false)
                .batched(true)
                .courseParticipants(participantRequests)
                .courseCodes(List.of(("RC" + tag).substring(0, Math.min(12, ("RC" + tag).length()))))
                .expectedClassSize("50")
                .build();
    }

    private ScheduleRequest toScheduleRequest(String tag, String courseCode, String classroomCode, int idx){
        int hour = 8 + idx;
        String suffix = hour >= 12 ? "pm" : "am";
        return ScheduleRequest.builder()
                .userId(tag)
                .courseCode(courseCode)
                .classroomCodes(List.of(classroomCode))
                .recurringDay("Monday")
                .startTime(hour + ":00" + suffix)
                .timeDuration("1")
                .build();
    }
}
