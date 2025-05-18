package com.project.server.db.services.college;

import com.project.server.db.models.College;
import com.project.server.dtos.request.CollegeRequest;
import com.project.server.exceptions.NotFoundException;

import java.util.List;

public interface CollegeService {
    boolean isExistingCollege(CollegeRequest request);
    College getByName(String name) throws NotFoundException;
    College getByCode(String code) throws NotFoundException;
    College save(College college);
    List<College> getAllColleges();
}
