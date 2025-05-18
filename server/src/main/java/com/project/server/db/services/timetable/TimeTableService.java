package com.project.server.db.services.timetable;

import com.project.server.db.models.TimeTable;
import com.project.server.dtos.request.TimetableRequest;
import com.project.server.exceptions.NotFoundException;
import com.project.server.exceptions.ScheduleUpsertException;

import java.util.Optional;

public interface TimeTableService {
    boolean activeTimeTableExists();
    boolean existsBySessionAndSemester(TimetableRequest request);
    TimeTable save(TimeTable timeTable);
    TimeTable findByCode(String code) throws NotFoundException;
    Optional<TimeTable> getActiveTimeTableOptional();
    TimeTable getActiveTimeTable() throws ScheduleUpsertException;
}
