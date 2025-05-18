package com.project.server.db.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "classrooms")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassRoom extends BaseModel{
    private String name;

    private Integer capacity;

    @ManyToOne
    @JoinColumn(name = "college_id", referencedColumnName = "id")
    private College college;

    @ManyToMany(mappedBy = "classRooms")
    private List<Schedule> schedules;
}
