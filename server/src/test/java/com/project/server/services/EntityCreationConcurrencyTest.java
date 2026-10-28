package com.project.server.services;

import com.project.server.BaseIntegrationTest;
import com.project.server.configs.tenant.TenantContext;
import com.project.server.db.models.OrgUnit;
import com.project.server.db.models.SchedulableUnit;
import com.project.server.db.repositories.OrgUnitRepository;
import com.project.server.db.repositories.SchedulableUnitRepository;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.dtos.request.course.CourseParticipantRequest;
import com.project.server.dtos.request.course.CourseRequest;
import com.project.server.enums.Type;
import com.project.server.exceptions.DuplicateEntryException;
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
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Same unprotected check-then-create race found in ScheduleMgtService and AuthMgtService, now
 * also closed in CollegeMgtService, DepartmentMgtService, and CourseMgtService — all three had
 * an isExisting*()-then-save() with no lock and no unique DB constraint beyond the internal
 * auto-generated code column. Isolated in its own class for the same reason as
 * ScheduleConcurrencyTest/UserRegistrationConcurrencyTest: real concurrency needs separate
 * threads/connections/transactions, so fixtures must be committed mid-test rather than relying
 * on @Transactional rollback — run-tagged unique names, explicit cleanup.
 */
@Slf4j
class EntityCreationConcurrencyTest extends BaseIntegrationTest {
    @Autowired
    private CollegeMgtService collegeMgtService;
    @Autowired
    private DepartmentMgtService departmentMgtService;
    @Autowired
    private CourseMgtService courseMgtService;
    @Autowired
    private OrgUnitRepository orgUnitRepository;
    @Autowired
    private SchedulableUnitRepository schedulableUnitRepository;

    @Test
    @SneakyThrows
    void should_allowOnlyOneConcurrentCollegeCreation_forTheSameName() {
        String tag = "collrace" + System.currentTimeMillis();
        runConcurrencyCheck(
                8,
                () -> collegeMgtService.createCollege(CollegeRequest.builder()
                        .collegeName(tag).collegeShortName(tag.substring(0, Math.min(15, tag.length()))).build()),
                () -> orgUnitRepository.findAll().stream().filter(o -> tag.equals(o.getName())).toList().size(),
                () -> orgUnitRepository.findAll().stream().filter(o -> tag.equals(o.getName())).forEach(orgUnitRepository::delete)
        );
    }

    @Test
    @SneakyThrows
    void should_allowOnlyOneConcurrentDepartmentCreation_forTheSameName() {
        String tag = "deptrace" + System.currentTimeMillis();
        runConcurrencyCheck(
                8,
                () -> departmentMgtService.createDepartment(DepartmentRequest.builder()
                        .departmentName(tag).departmentShortName(tag.substring(0, Math.min(15, tag.length()))).build()),
                () -> orgUnitRepository.findAll().stream().filter(o -> tag.equals(o.getName())).toList().size(),
                () -> orgUnitRepository.findAll().stream().filter(o -> tag.equals(o.getName())).forEach(orgUnitRepository::delete)
        );
    }

    @Test
    @SneakyThrows
    void should_allowOnlyOneConcurrentCourseCreation_forTheSameTitle() {
        String tag = "courserace" + System.currentTimeMillis();
        runConcurrencyCheck(
                8,
                () -> courseMgtService.upsertCourse(CourseRequest.builder()
                        .courseTitle(tag)
                        .courseUnit("2")
                        .general(true)
                        .shared(false)
                        .batched(false)
                        .courseParticipants(List.of())
                        .courseCodes(List.of(tag.substring(0, Math.min(10, tag.length()))))
                        .expectedClassSize("30")
                        .build(), Type.CREATE, null),
                () -> schedulableUnitRepository.findAll().stream().filter(c -> tag.equals(c.getTitle())).toList().size(),
                () -> schedulableUnitRepository.findAll().stream().filter(c -> tag.equals(c.getTitle())).forEach(schedulableUnitRepository::delete)
        );
    }

    @SneakyThrows
    private void runConcurrencyCheck(int concurrency, ThrowingRunnable action, Supplier<Integer> rowCounter, Runnable cleanup) {
        Long tenantId = TenantContext.get();
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
                pool.execute(() -> {
                    TenantContext.set(tenantId);
                    try {
                        ready.countDown();
                        try { go.await(); } catch (InterruptedException ignored) {}
                        try {
                            action.run();
                            successes.incrementAndGet();
                        } catch (DuplicateEntryException e) {
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
            assertEquals(1, successes.get(), "Exactly one concurrent create should succeed");
            assertEquals(concurrency - 1, conflicts.get());
            assertEquals(1, (int) rowCounter.get(), "Exactly one row should exist in the DB");
        } finally {
            try {
                cleanup.run();
            } catch (Exception e) {
                log.warn("Entity creation race test cleanup did not fully complete: {}", e.getMessage());
            }
            TenantContext.set(tenantId);
            TestTransaction.start();
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
