package com.project.server.services;

import com.project.server.BaseIntegrationTest;
import com.project.server.configs.tenant.TenantContext;
import com.project.server.db.models.AppUser;
import com.project.server.db.repositories.AppUserRepository;
import com.project.server.dtos.request.auth.StaffSignUpRequest;
import com.project.server.exceptions.DuplicateEntryException;
import com.project.server.exceptions.NotFoundException;
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
 * Isolated in its own class for the same reason as ScheduleConcurrencyTest: real concurrency
 * needs separate threads/connections/transactions, so fixtures have to be committed mid-test
 * (TestTransaction.end()) rather than relying on @Transactional rollback — run-tagged unique
 * names throughout, explicit cleanup, no collision with any other test.
 */
@Slf4j
class UserRegistrationConcurrencyTest extends BaseIntegrationTest {
    @Autowired
    private AuthMgtService authMgtService;
    @Autowired
    private AppUserRepository appUserRepository;

    /**
     * isExistingUser()-then-save() in AuthMgtService.createUser had no lock and no unique DB
     * constraint on user_id/user_email either. Found via k6 load testing: 10 concurrent
     * registrations with the identical staffId all succeeded, creating 10 duplicate rows that
     * then permanently 500'd that user's own login. Fixed with registration locks
     * (AuthMgtService.acquireRegistrationLocks) plus a unique DB constraint as defense in depth.
     */
    @Test
    @SneakyThrows
    void should_allowOnlyOneConcurrentRegistration_forTheSameStaffId() {
        String tag = "regrace" + System.currentTimeMillis();
        int concurrency = 8;
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
                            authMgtService.createUser(StaffSignUpRequest.builder()
                                    .staffEmail(tag + "@load.test")
                                    .password("password")
                                    .staffId(tag)
                                    .build());
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
            assertEquals(1, successes.get(), "Exactly one concurrent registration should succeed for one staffId");
            assertEquals(concurrency - 1, conflicts.get());

            List<AppUser> rows = appUserRepository.findAll().stream()
                    .filter(u -> tag.toUpperCase().equals(u.getUserId()))
                    .toList();
            assertEquals(1, rows.size(), "Exactly one AppUser row should exist for this staffId");
        } finally {
            cleanup(tag);
            TenantContext.set(tenantId);
            TestTransaction.start();
        }
    }

    private void cleanup(String tag) {
        try {
            appUserRepository.findByUserId(tag.toUpperCase()).ifPresent(appUserRepository::delete);
        } catch (Exception e) {
            log.warn("Registration race test cleanup did not fully complete: {}", e.getMessage());
        }
    }
}
