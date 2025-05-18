package com.project.server.db.services.department;

import com.project.server.db.models.Department;
import com.project.server.db.repositories.DepartmentRepository;
import com.project.server.dtos.request.DepartmentRequest;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService{
    private final DepartmentRepository repository;

    @Override
    public boolean isExistingDepartment(DepartmentRequest request) {
        return repository.findByName(request.getDepartmentName(), request.getDepartmentShortName()).isPresent();
    }

    @Override
    public Department getByName(String name) throws NotFoundException {
        return repository.findByName(name, null).orElseThrow(()-> new NotFoundException("Department not found."));
    }

    @Override
    public Department getByCode(String code) throws NotFoundException {
        return repository.findByCode(code).orElseThrow(()-> new NotFoundException("Department not found."));
    }

    @Override
    public void save(Department department) {
        repository.save(department);
    }

    @Override
    public List<Department> getAllByCollege(String college) {
        return repository.findAllByCollege(college);
    }

    @Override
    public List<Department> getAllByMatch(String name) {
        return repository.findAllByMatch(name);
    }
}
