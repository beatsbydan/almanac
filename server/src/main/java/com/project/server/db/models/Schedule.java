package com.project.server.db.models;

import com.project.server.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Builder
@Table(name = "schedules")
@NoArgsConstructor
@AllArgsConstructor
public class Schedule extends BaseModel{
    private String day;

    private String timePeriod;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleCombination> scheduleCombinations;

    @OneToOne
    @JoinColumn(
            name = "course_id",
            referencedColumnName = "id"
    )
    private Course course;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id"
    )
    private AppUser appUser;

    @ManyToOne
    @JoinColumn(
            name = "timetable_id",
            referencedColumnName = "id"
    )
    private TimeTable timeTable;

    @ManyToMany
    @JoinTable(
            name = "schedule_classrooms",
            joinColumns = @JoinColumn(name = "schedule_id"),
            inverseJoinColumns = @JoinColumn(name = "classroom_id")
    )
    private List<ClassRoom> classRooms;

}