package com.project.server.db.services.orgUnit;

import com.project.server.db.models.OrgUnit;
import com.project.server.db.repositories.OrgUnitRepository;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgUnitServiceImpl implements OrgUnitService {
    private final OrgUnitRepository repository;

    @Override
    public OrgUnit save(OrgUnit orgUnit) {
        return repository.save(orgUnit);
    }

    @Override
    public OrgUnit getByCode(String code) throws NotFoundException {
        return repository.findByCode(code).orElseThrow(() -> new NotFoundException("Org unit not found."));
    }

    @Override
    public List<OrgUnit> getRoots() {
        return repository.findByParentIsNull();
    }

    @Override
    public List<OrgUnit> getChildren(String parentCode) throws NotFoundException {
        return repository.findByParent(getByCode(parentCode));
    }

    @Override
    public boolean isExistingRoot(String name, String shortName) {
        return repository.findRootByNames(name, shortName).isPresent();
    }

    @Override
    public boolean isExistingChild(String name, String shortName) {
        return repository.findChildByNames(name, shortName).isPresent();
    }

    @Override
    public OrgUnit getRootByName(String name) throws NotFoundException {
        return repository.findRootByNames(name, null).orElseThrow(() -> new NotFoundException("Org unit not found."));
    }

    @Override
    public OrgUnit getChildByName(String name) throws NotFoundException {
        return repository.findChildByNames(name, null).orElseThrow(() -> new NotFoundException("Org unit not found."));
    }

    @Override
    public List<OrgUnit> getAllByParentName(String parentName) {
        return repository.findAllByParentName(parentName);
    }

    @Override
    public List<OrgUnit> getAllByMatch(String name) {
        return repository.findAllByMatch(name);
    }
}
