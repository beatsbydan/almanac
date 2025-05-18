package com.project.server.db.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "course_participants")
@NoArgsConstructor
@AllArgsConstructor
public class CourseParticipant extends BaseModel{
    @OneToOne
    @JoinColumn(name = "level_id", referencedColumnName = "id")
    private Level level;

    @ManyToMany
    @JoinTable(
            name = "course_participant_departments",
            joinColumns = @JoinColumn(name = "course_participant_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private List<Department> departments;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private Course course;
}
