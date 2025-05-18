package com.project.server.db.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "departments")
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseModel{
    private String name;

    private String shortName;

    @ManyToOne
    @JoinColumn(name = "college_id", referencedColumnName = "id")
    private College college;

    @ManyToMany(mappedBy = "departments")
    private List<CourseParticipant> courseParticipants;
}
