package com.project.server.db.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@Table(name = "course_codes")
@NoArgsConstructor
@AllArgsConstructor
public class CourseCode extends BaseModel{
    private String courseCode;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private Course course;
}
