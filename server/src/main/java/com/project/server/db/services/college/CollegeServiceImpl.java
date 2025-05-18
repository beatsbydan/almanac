package com.project.server.db.services.college;

import com.project.server.db.models.College;
import com.project.server.db.repositories.CollegeRepository;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeServiceImpl implements CollegeService{
    private final CollegeRepository repository;

    @Override
    public boolean isExistingCollege(CollegeRequest request) {
        return repository.findByNames(request.getCollegeName(), request.getCollegeShortName()).isPresent();
    }

    @Override
    public College getByName(String name) throws NotFoundException {
        return repository.findByName(name).orElseThrow(()-> new NotFoundException("College not found."));
    }

    @Override
    public College getByCode(String code) throws NotFoundException {
        return repository.findByCode(code).orElseThrow(()-> new NotFoundException("College not found."));
    }

    @Override
    public College save(College college) {
        return repository.save(college);
    }

    @Override
    public List<College> getAllColleges() {
        return repository.findAll();
    }
}
