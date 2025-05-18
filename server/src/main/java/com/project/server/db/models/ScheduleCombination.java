package com.project.server.db.models;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "schedule_combinations")
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCombination extends BaseModel {
    private String combination;

    private String day;

    private Integer startTime;

    private Integer endTime;

    private String classroomName;

    @ManyToOne
    @JoinColumn(name = "schedule_id", referencedColumnName = "id")
    private Schedule schedule;
}
