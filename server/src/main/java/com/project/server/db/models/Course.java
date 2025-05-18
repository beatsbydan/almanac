package com.project.server.db.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "courses")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseModel{
    private String courseTitle;

    private Integer courseUnit;

    private boolean shared;

    private boolean batched;

    private boolean general;

    private Integer expectedClassSize;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CourseCode> courseCodes = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CourseParticipant> courseParticipants = new ArrayList<>();;
}