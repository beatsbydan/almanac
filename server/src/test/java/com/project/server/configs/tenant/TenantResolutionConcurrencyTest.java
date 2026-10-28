package com.project.server.configs.tenant;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.Tenant;
import com.project.server.db.repositories.OrgUnitRepository;
import com.project.server.db.repositories.TenantRepository;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.enums.InstitutionType;
import com.project.server.enums.Status;
import com.project.server.services.CollegeMgtService;
import com.project.server.services.DepartmentMgtService;
import com.project.server.util.VariableUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TenantIsolationTest proves Hibernate's @TenantId filter works for ONE thread switching
 * TenantContext by hand. It does not prove TenantResolutionFilter correctly isolates tenants when
 * real Tomcat worker threads are reused across requests from DIFFERENT tenants under concurrency —
 * the filter's set/clear(finally) runs once per request, so a bug there (an early return that
 * skips clear(), an exception path that skips set()) would only ever surface as cross-tenant data
 * bleeding into a DIFFERENT concurrently-reused thread's response, never in a single-threaded test.
 *
 * Hits a real public, tenant-scoped GET endpoint (/api/v1/departments, permitAll in SecurityConfig
 * but still behind TenantResolutionFilter, which runs for every request regardless of
 * authorization outcome) through MockMvc from a small worker pool, alternating the X-Tenant-Slug
 * header between two tenants that each have a department by a DIFFERENT name under a college with
 * the SAME name — any ThreadLocal leak shows up as tenant A's request returning tenant B's
 * department, or vice versa.
 *
 * BaseIntegrationTest's inherited `mockMvc` is built via plain webAppContextSetup(...).build(),
 * which does NOT register TenantResolutionFilter (it's wired into the security filter chain, not
 * auto-detected by a bare MockMvc builder) — every request through it silently falls back to
 * CurrentTenantResolver's bootstrap tenant regardless of header, which happens to BE tenant A, so
 * every existing controller test passes without ever actually exercising the filter. This test
 * builds its own MockMvc with the filter explicitly attached so the real per-request
 * resolution/clear path is what's under test.
 */
@Slf4j
class TenantResolutionConcurrencyTest extends BaseIntegrationTest {
    @Autowired
    private CollegeMgtService collegeMgtService;
    @Autowired
    private DepartmentMgtService departmentMgtService;
    @Autowired
    private OrgUnitRepository orgUnitRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private TenantResolutionFilter tenantResolutionFilter;

    private MockMvc tenantAwareMockMvc;

    @BeforeEach
    void setUpTenantAwareMockMvc() {
        tenantAwareMockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(tenantResolutionFilter)
                .build();
    }

    @Test
    @SneakyThrows
    void concurrentRequestsFromDifferentTenants_neverCrossContaminateAcrossReusedThreads() {
        Long tenantAId = TenantContext.get();
        String tag = "tenantisoconc" + System.currentTimeMillis();
        // Short names are capped at 15 chars and only used for the uniqueness check — a prefix of
        // the tag barely changes across runs seconds apart (epoch millis' leading digits are
        // stable for millennia), so a truncated tag collides with a PRIOR run's leftover row
        // (exactly what happened the first time this ran). nanoTime's low-order digits actually
        // vary run to run.
        String shortSuffix = Long.toString(System.nanoTime() % 1_000_000_000L, 36);
        String collegeName = tag;
        String deptNameA = tag + "-A";
        String deptNameB = tag + "-B";
        String tenantBSlug = tag + "-b";

        collegeMgtService.createCollege(CollegeRequest.builder()
                .collegeName(collegeName).collegeShortName("c" + shortSuffix).build());
        departmentMgtService.createDepartment(DepartmentRequest.builder()
                .departmentName(deptNameA).departmentShortName("da" + shortSuffix)
                .college(collegeName).build());

        Tenant tenantB = tenantRepository.save(Tenant.builder()
                .slug(tenantBSlug).name(tag + " Tenant B")
                .institutionType(InstitutionType.SECONDARY).status(Status.ACTIVATED).build());

        // Hibernate resolves the tenant filter once per session and caches it for the session's
        // whole lifetime (see TenantIsolationTest) — switching TenantContext mid-session has no
        // effect, so tenant B's creates below would otherwise silently run as tenant A and collide
        // with the college just created above. end()/start() forces a fresh session per switch.
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TenantContext.set(tenantB.getId());
        TestTransaction.start();
        collegeMgtService.createCollege(CollegeRequest.builder()
                .collegeName(collegeName).collegeShortName("c" + shortSuffix).build());
        departmentMgtService.createDepartment(DepartmentRequest.builder()
                .departmentName(deptNameB).departmentShortName("db" + shortSuffix)
                .college(collegeName).build());
        TenantContext.set(tenantAId);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        try {
            int concurrency = 6;
            int totalRequests = 180;
            ExecutorService pool = Executors.newFixedThreadPool(concurrency);
            CountDownLatch go = new CountDownLatch(1);
            AtomicInteger violations = new AtomicInteger();
            List<String> violationDetails = new ArrayList<>();

            List<Runnable> tasks = new ArrayList<>();
            for (int i = 0; i < totalRequests; i++) {
                boolean isTenantA = i % 2 == 0;
                tasks.add(() -> {
                    try {
                        go.await();
                        String slug = isTenantA ? VariableUtil.DEFAULT_TENANT_SLUG : tenantBSlug;
                        String expectedDept = isTenantA ? deptNameA : deptNameB;
                        String forbiddenDept = isTenantA ? deptNameB : deptNameA;

                        String body = tenantAwareMockMvc.perform(MockMvcRequestBuilders.get("/api/v1/departments")
                                        .param("key", collegeName)
                                        .header(VariableUtil.TENANT_SLUG_HEADER, slug))
                                .andReturn().getResponse().getContentAsString();

                        if (!body.contains(expectedDept) || body.contains(forbiddenDept)) {
                            violations.incrementAndGet();
                            synchronized (violationDetails) {
                                violationDetails.add("slug=" + slug + " expected=" + expectedDept + " body=" + body);
                            }
                        }
                    } catch (Exception e) {
                        violations.incrementAndGet();
                        synchronized (violationDetails) {
                            violationDetails.add("exception: " + e);
                        }
                    }
                });
            }
            tasks.forEach(pool::execute);
            go.countDown();
            pool.shutdown();
            assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS), "Workers did not finish in time");

            assertEquals(0, violations.get(), "Cross-tenant contamination detected: " + violationDetails);
        } finally {
            cleanup(tenantAId, tenantB, collegeName);
        }
    }

    private void cleanup(Long tenantAId, Tenant tenantB, String collegeName) {
        try {
            TenantContext.set(tenantAId);
            deleteTaggedOrgUnits(collegeName);

            TenantContext.set(tenantB.getId());
            deleteTaggedOrgUnits(collegeName);

            tenantRepository.delete(tenantB);
        } catch (Exception e) {
            log.warn("Tenant resolution concurrency test cleanup did not fully complete: {}", e.getMessage());
        } finally {
            TenantContext.set(tenantAId);
            TestTransaction.start();
        }
    }

    /** Departments must go before their parent college — the FK won't allow the reverse order. */
    private void deleteTaggedOrgUnits(String collegeName) {
        orgUnitRepository.findAll().stream()
                .filter(o -> o.getName().startsWith(collegeName))
                .sorted((a, b) -> Boolean.compare(a.getParent() == null, b.getParent() == null))
                .forEach(orgUnitRepository::delete);
    }
}
