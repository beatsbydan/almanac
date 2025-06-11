package com.project.server.db.models;

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

    private Integer startTime;

    private Integer endTime;

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

    public int getStartTime(){
        String[] timeSplit = this.getTimePeriod().split(" - ");
        return Integer.parseInt(timeSplit[0].split(":00")[0]);
    }

    public int getEndTime(){
        String[] timeSplit = this.getTimePeriod().split(" - ");
        return Integer.parseInt(timeSplit[1].split(":00")[1]);
    }

}