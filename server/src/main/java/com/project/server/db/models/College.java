package com.project.server.db.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@Builder
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "colleges")
@NoArgsConstructor
@AllArgsConstructor
public class College extends BaseModel{
    private String name;

    private String shortName;

    @OneToMany(mappedBy = "college")
    private List<Department> departments;
}
