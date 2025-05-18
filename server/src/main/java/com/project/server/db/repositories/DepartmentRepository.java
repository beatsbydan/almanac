package com.project.server.db.repositories;

import com.project.server.db.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query("SELECT d FROM Department d WHERE d.name = :name OR d.shortName = :shortName")
    Optional<Department> findByName(@Param("name") String name, @Param("shortName") String shortName);
    Optional<Department> findByCode(String code);

    @Query("SELECT d FROM Department d WHERE d.college.shortName = :collegeShortName")
    List<Department> findAllByCollege(@Param("collegeShortName") String college);

    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Department> findAllByMatch(@Param("name") String name);
}
