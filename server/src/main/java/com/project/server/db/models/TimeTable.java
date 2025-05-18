package com.project.server.db.models;

import com.project.server.enums.Semester;
import com.project.server.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "timetables")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeTable extends BaseModel{
    private String fileName;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING_REPORT_GENERATION;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status creationStatus = Status.PAUSED;

    @OneToMany(mappedBy = "timeTable")
    private List<Schedule> schedules;

    @Builder.Default
    private boolean active = true;

    private String session;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    private LocalDate creationStartDate;

    private LocalDate creationEndDate;
}
