package com.project.server.db.services.classroom;

import com.project.server.db.models.ClassRoom;
import com.project.server.db.repositories.ClassRoomRepository;
import com.project.server.dtos.request.ClassroomRequest;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassRoomServiceImpl implements ClassRoomService{
    private final ClassRoomRepository repository;

    @Override
    public boolean isExistingClassroom(ClassroomRequest request) {
        return repository.findByName(request.getClassName()).isPresent();
    }

    @Override
    public void save(ClassRoom classRoom) {
        repository.save(classRoom);
    }

    @Override
    public ClassRoom findByCode(String code) throws NotFoundException {
        return repository.findByCode(code).orElseThrow(() -> new NotFoundException("Classroom not found."));
    }

    @Override
    public List<ClassRoom> findAllByCollege(String college) {
        return repository.findAllByCollege(college);
    }

    @Override
    public List<ClassRoom> filterByCollege(String college) {
        return repository.filterByCollege(college);
    }

    @Override
    public List<ClassRoom> getClassroomsWithoutCollege() {
        return repository.findAllWithoutCollege();
    }

    @Override
    public List<ClassRoom> filterByNameOrCollege(String key) {
        return repository.filterByNameOrCollege(key);
    }

    @Override
    public List<ClassRoom> getAll() {
        return repository.findAll();
    }
}
