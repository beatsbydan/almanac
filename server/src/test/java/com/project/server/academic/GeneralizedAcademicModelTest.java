package com.project.server.academic;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.*;
import com.project.server.db.services.level.LevelService;
import com.project.server.db.services.orgUnit.OrgUnitService;
import com.project.server.db.services.schedulableUnit.SchedulableUnitService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves OrgUnit/SchedulableUnit can represent two structurally different institutions with the
 * same model: a university's two-level College -> Department hierarchy with level-scoped course
 * participants, and a K-12 school's flat, single-level Class with no separate Level concept at
 * all. Tenant-row isolation itself is already proven generically by TenantIsolationTest — this
 * test is specifically about the hierarchy/participant shape, not re-proving the tenancy
 * mechanism per entity.
 */
class GeneralizedAcademicModelTest extends BaseIntegrationTest {
    @Autowired
    private OrgUnitService orgUnitService;
    @Autowired
    private SchedulableUnitService schedulableUnitService;
    @Autowired
    private LevelService levelService;

    @Test
    @SneakyThrows
    void university_shape_twoLevelHierarchy_withLevelScopedParticipant() {
        OrgUnit college = orgUnitService.save(OrgUnit.builder()
                .name("College of Computing").shortName("COC").type("College").build());
        OrgUnit department = orgUnitService.save(OrgUnit.builder()
                .name("Computer Science").shortName("CSC").type("Department").parent(college).build());

        assertNull(college.getParent());
        List<OrgUnit> children = orgUnitService.getChildren(college.getCode());
        assertEquals(1, children.size());
        assertEquals(department.getCode(), children.getFirst().getCode());

        Level level100 = levelService.save(Level.builder().level("100").build());

        SchedulableUnit course = schedulableUnitService.save(SchedulableUnit.builder()
                .title("Intro to Programming")
                .unit(3)
                .expectedClassSize(50)
                .build());

        SchedulableUnitParticipant participant = SchedulableUnitParticipant.builder()
                .schedulableUnit(course)
                .level(level100)
                .orgUnits(new ArrayList<>(List.of(department)))
                .build();
        course.setParticipants(new ArrayList<>(List.of(participant)));
        schedulableUnitService.save(course);

        SchedulableUnit fetched = schedulableUnitService.getByCode(course.getCode());
        assertEquals(1, fetched.getParticipants().size());
        SchedulableUnitParticipant fetchedParticipant = fetched.getParticipants().getFirst();
        assertEquals("100", fetchedParticipant.getLevel().getLevel());
        assertEquals(1, fetchedParticipant.getOrgUnits().size());
        assertEquals(department.getCode(), fetchedParticipant.getOrgUnits().getFirst().getCode());
    }

    @Test
    @SneakyThrows
    void k12_shape_flatSingleLevelHierarchy_withNoLevelConcept() {
        OrgUnit schoolClass = orgUnitService.save(OrgUnit.builder()
                .name("JSS 1").shortName("JSS1").type("Class").build());

        assertNull(schoolClass.getParent());
        assertTrue(orgUnitService.getChildren(schoolClass.getCode()).isEmpty());

        SchedulableUnit subject = schedulableUnitService.save(SchedulableUnit.builder()
                .title("Basic Science")
                .expectedClassSize(35)
                .build());

        SchedulableUnitParticipant participant = SchedulableUnitParticipant.builder()
                .schedulableUnit(subject)
                .level(null)
                .orgUnits(new ArrayList<>(List.of(schoolClass)))
                .build();
        subject.setParticipants(new ArrayList<>(List.of(participant)));
        schedulableUnitService.save(subject);

        SchedulableUnit fetched = schedulableUnitService.getByCode(subject.getCode());
        assertEquals(1, fetched.getParticipants().size());
        SchedulableUnitParticipant fetchedParticipant = fetched.getParticipants().getFirst();
        assertNull(fetchedParticipant.getLevel(), "K-12 participant should not need a Level — the Class org unit already encodes the stage");
        assertEquals(schoolClass.getCode(), fetchedParticipant.getOrgUnits().getFirst().getCode());
    }
}
