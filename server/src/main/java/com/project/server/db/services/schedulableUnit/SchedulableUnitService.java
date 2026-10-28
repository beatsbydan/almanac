package com.project.server.db.services.schedulableUnit;

import com.project.server.db.models.SchedulableUnit;
import com.project.server.db.models.SchedulableUnitParticipant;
import com.project.server.exceptions.NotFoundException;

import java.util.List;

public interface SchedulableUnitService {
    SchedulableUnit save(SchedulableUnit schedulableUnit);
    SchedulableUnit getByCode(String code) throws NotFoundException;
    SchedulableUnit getByCodeWithParticipants(String code) throws NotFoundException;
    SchedulableUnit getByTitle(String title) throws NotFoundException;
    List<SchedulableUnit> getAll();
    boolean isExistingByTitle(String title);
    List<SchedulableUnit> getAllGeneral();
    List<SchedulableUnit> getByFilterParams(
            int unit,
            String orgUnitName,
            String levelCode,
            boolean shared,
            boolean batched,
            boolean general
    );
    List<SchedulableUnit> filterByKey(String key);
    boolean isExistingCode(String unitCode);
    List<SchedulableUnitParticipant> getParticipants(String unitCode);
}
