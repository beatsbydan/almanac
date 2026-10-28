package com.project.server.db.services.schedulableUnit;

import com.project.server.db.models.SchedulableUnit;
import com.project.server.db.models.SchedulableUnitParticipant;
import com.project.server.db.repositories.SchedulableUnitCodeRepository;
import com.project.server.db.repositories.SchedulableUnitParticipantRepository;
import com.project.server.db.repositories.SchedulableUnitRepository;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulableUnitServiceImpl implements SchedulableUnitService {
    private final SchedulableUnitRepository repository;
    private final SchedulableUnitCodeRepository codeRepository;
    private final SchedulableUnitParticipantRepository participantRepository;

    @Override
    public SchedulableUnit save(SchedulableUnit schedulableUnit) {
        return repository.save(schedulableUnit);
    }

    @Override
    public SchedulableUnit getByCode(String code) throws NotFoundException {
        return repository.findByCode(code).orElseThrow(() -> new NotFoundException("This course does not exist."));
    }

    @Override
    public SchedulableUnit getByCodeWithParticipants(String code) throws NotFoundException {
        return repository.findByCodeWithParticipants(code).orElseThrow(() -> new NotFoundException("This course does not exist."));
    }

    @Override
    public SchedulableUnit getByTitle(String title) throws NotFoundException {
        return repository.findByTitle(title).orElseThrow(() -> new NotFoundException("This course does not exist."));
    }

    @Override
    public List<SchedulableUnit> getAll() {
        return repository.findAll();
    }

    @Override
    public boolean isExistingByTitle(String title) {
        return repository.findByTitle(title).isPresent();
    }

    @Override
    public List<SchedulableUnit> getAllGeneral() {
        return repository.findAllGeneral();
    }

    @Override
    public List<SchedulableUnit> getByFilterParams(
            int unit,
            String orgUnitName,
            String levelCode,
            boolean shared,
            boolean batched,
            boolean general
    ) {
        return repository.findAllByFilterParams(unit, orgUnitName, levelCode, shared, batched, general);
    }

    @Override
    public List<SchedulableUnit> filterByKey(String key) {
        return repository.filterByKey(key);
    }

    @Override
    public boolean isExistingCode(String unitCode) {
        return codeRepository.findByUnitCode(unitCode).isPresent();
    }

    @Override
    public List<SchedulableUnitParticipant> getParticipants(String unitCode) {
        return participantRepository.findByUnit(unitCode);
    }
}
