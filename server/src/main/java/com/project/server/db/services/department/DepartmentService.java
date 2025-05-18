package com.project.server.db.services.department;

import com.project.server.db.models.Department;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.exceptions.NotFoundException;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    boolean isExistingDepartment (DepartmentRequest request);
    Department getByName(String name) throws NotFoundException;
    Department getByCode(String code) throws NotFoundException;
    void save(Department department);
    List<Department> getAllByCollege(String college);
    List<Department> getAllByMatch(String name);
}
