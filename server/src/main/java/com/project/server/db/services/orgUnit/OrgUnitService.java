package com.project.server.db.services.orgUnit;

import com.project.server.db.models.OrgUnit;
import com.project.server.exceptions.NotFoundException;

import java.util.List;

public interface OrgUnitService {
    OrgUnit save(OrgUnit orgUnit);
    OrgUnit getByCode(String code) throws NotFoundException;
    List<OrgUnit> getRoots();
    List<OrgUnit> getChildren(String parentCode) throws NotFoundException;
    boolean isExistingRoot(String name, String shortName);
    boolean isExistingChild(String name, String shortName);
    OrgUnit getRootByName(String name) throws NotFoundException;
    OrgUnit getChildByName(String name) throws NotFoundException;
    List<OrgUnit> getAllByParentName(String parentName);
    List<OrgUnit> getAllByMatch(String name);
}
