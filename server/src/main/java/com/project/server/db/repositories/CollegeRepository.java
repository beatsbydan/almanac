package com.project.server.db.repositories;

import com.project.server.db.models.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {
    @Query("SELECT c FROM College c WHERE c.name = :name OR c.shortName = :name")
    Optional<College> findByName(@Param("name") String name);

    @Query("SELECT c FROM College c WHERE c.name = :name OR c.shortName = :shortName")
    Optional<College> findByNames(@Param("name") String name, @Param("shortName") String shortName);
    Optional<College> findByCode(String code);
}
