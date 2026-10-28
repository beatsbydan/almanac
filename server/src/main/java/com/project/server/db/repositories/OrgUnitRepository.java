package com.project.server.db.repositories;

import com.project.server.db.models.OrgUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrgUnitRepository extends JpaRepository<OrgUnit, Long> {
    Optional<OrgUnit> findByCode(String code);
    List<OrgUnit> findByParentIsNull();
    List<OrgUnit> findByParent(OrgUnit parent);

    @Query("SELECT o FROM OrgUnit o WHERE o.type = 'College' AND (o.name = :name OR o.shortName = :shortName)")
    Optional<OrgUnit> findRootByNames(@Param("name") String name, @Param("shortName") String shortName);

    @Query("SELECT o FROM OrgUnit o WHERE o.type = 'Department' AND (o.name = :name OR o.shortName = :shortName)")
    Optional<OrgUnit> findChildByNames(@Param("name") String name, @Param("shortName") String shortName);

    @Query("SELECT o FROM OrgUnit o WHERE o.parent.shortName = :parentName OR o.parent.name = :parentName")
    List<OrgUnit> findAllByParentName(@Param("parentName") String parentName);

    @Query("SELECT o FROM OrgUnit o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<OrgUnit> findAllByMatch(@Param("name") String name);
}
