package com.project.server.configs.tenant;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.OrgUnit;
import com.project.server.db.models.Tenant;
import com.project.server.db.repositories.OrgUnitRepository;
import com.project.server.db.repositories.TenantRepository;
import com.project.server.db.services.orgUnit.OrgUnitService;
import com.project.server.db.services.tenant.TenantService;
import com.project.server.enums.InstitutionType;
import com.project.server.enums.Status;
import com.project.server.exceptions.NotFoundException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every other integration test only ever runs under the single default tenant, so passing them
 * proves backward compatibility, not isolation. This test is the one place that actually proves
 * Hibernate's @TenantId filtering does what it's supposed to: a row created under one tenant must
 * be completely invisible under another.
 *
 * Switching tenants mid-test requires a fresh Hibernate session — the resolved tenant is cached
 * for a session's whole lifetime, so TenantContext changes only take effect on the *next* session.
 * TestTransaction.end()/start() is used to force that, with explicit commits since the data has to
 * actually persist across the session boundary for the check to mean anything; cleanup undoes it
 * afterward.
 */
@Slf4j
class TenantIsolationTest extends BaseIntegrationTest {
    @Autowired
    private TenantService tenantService;
    @Autowired
    private OrgUnitService orgUnitService;
    @Autowired
    private OrgUnitRepository orgUnitRepository;
    @Autowired
    private TenantRepository tenantRepository;

    @Test
    @SneakyThrows
    void collegeCreatedUnderOneTenant_isInvisibleUnderAnotherTenant() {
        Long tenantAId = TenantContext.get();

        OrgUnit collegeA = orgUnitService.save(OrgUnit.builder().name("Isolation Test College A").shortName("ISOA").type("College").build());
        Tenant tenantB = tenantService.save(Tenant.builder()
                .slug("isolation-test-tenant-b")
                .name("Isolation Test Tenant B")
                .institutionType(InstitutionType.SECONDARY)
                .status(Status.ACTIVATED)
                .build());

        TestTransaction.flagForCommit();
        TestTransaction.end();

        try {
            TenantContext.set(tenantB.getId());
            TestTransaction.start();

            List<OrgUnit> visibleToTenantB = orgUnitService.getRoots();
            assertTrue(
                    visibleToTenantB.stream().noneMatch(c -> c.getCode().equals(collegeA.getCode())),
                    "An org unit created under tenant A must not appear in tenant B's list"
            );
            assertThrows(
                    NotFoundException.class,
                    () -> orgUnitService.getByCode(collegeA.getCode()),
                    "Fetching tenant A's org unit by code while scoped to tenant B must fail"
            );
        }
        finally {
            cleanup(tenantAId, collegeA, tenantB);
        }
    }

    private void cleanup(Long tenantAId, OrgUnit collegeA, Tenant tenantB) {
        try {
            TestTransaction.end();
            TenantContext.set(tenantAId);
            TestTransaction.start();
            orgUnitRepository.delete(collegeA);
            tenantRepository.delete(tenantB);
            TestTransaction.flagForCommit();
            TestTransaction.end();
            TestTransaction.start();
        }
        catch (Exception e) {
            log.warn("Isolation test cleanup did not fully complete: {}", e.getMessage());
        }
    }
}
