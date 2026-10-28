package de.szut.pms.project;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Datenbankzugriff für Projekte. save, findAll, findById, deleteById, existsById …
 * erbt das Interface von JpaRepository – Spring erzeugt die Implementierung.
 */
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    /**
     * Sucht alle anderen Projekte, in denen der Mitarbeiter im Zeitraum startDate bis endDate
     * schon eingeplant ist. Das Projekt mit der Id projectId wird dabei nicht mitgezählt.
     * <p>
     * Zwei Zeiträume überschneiden sich, wenn: anderer Start <= unser Ende UND anderes Ende >= unser Start.
     * Weil wir <= und >= benutzen, zählt auch ein gleicher Tag als Überschneidung
     * (Ende Projekt A = Start Projekt B → Konflikt).
     */
    @Query("""
            SELECT p FROM ProjectEntity p
            JOIN p.employees e
            WHERE e.employeeId = :employeeId
              AND p.id <> :projectId
              AND p.startDate <= :endDate
              AND p.endDate >= :startDate
            """)
    List<ProjectEntity> findOverlappingProjects(@Param("employeeId") Long employeeId,
                                                @Param("projectId") Long projectId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}
