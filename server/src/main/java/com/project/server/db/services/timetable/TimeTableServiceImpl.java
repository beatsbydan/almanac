package com.project.server.db.services.timetable;

import com.project.server.db.models.TimeTable;
import com.project.server.db.repositories.TimeTableRepository;
import com.project.server.dtos.request.TimetableRequest;
import com.project.server.enums.Semester;
import com.project.server.exceptions.NotFoundException;
import com.project.server.exceptions.ScheduleUpsertException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimeTableServiceImpl implements TimeTableService{
    private final TimeTableRepository repository;

    @Override
    public boolean activeTimeTableExists() {
        return repository.findActiveTimetable().isPresent();
    }

    @Override
    public boolean existsBySessionAndSemester(TimetableRequest request) {
        return repository.findBySessionAndSemester(request.getSession(), Semester.valueOf(request.getSemester())).isPresent();
    }

    @Override
    public TimeTable save(TimeTable timeTable) {
        return repository.save(timeTable);
    }

    @Override
    public TimeTable findByCode(String code) throws NotFoundException {
        return repository.findByCode(code).orElseThrow(()-> new NotFoundException("Timetable not found."));
    }

    @Override
    public Optional<TimeTable> getActiveTimeTableOptional() {
        return repository.findActiveTimetable();
    }

    @Override
    public TimeTable getActiveTimeTable() throws ScheduleUpsertException {
        return repository.findActiveTimetable().orElseThrow(()-> new ScheduleUpsertException("You cannot create a schedule at this time."));
    }
}
