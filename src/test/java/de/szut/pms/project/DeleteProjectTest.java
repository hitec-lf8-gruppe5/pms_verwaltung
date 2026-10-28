package de.szut.pms.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

/** Integrationstests für die Story "Projekte löschen" (DELETE /projects/{id}). */
class DeleteProjectTest extends AbstractIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @WithMockUser
    void deleteProjectWithAssignedEmployeeReturnsNoContentAndRemovesProject() throws Exception {
        // Projekt mit einem zugeordneten Mitarbeiter direkt über das Repository anlegen.
        ProjectEntity project = new ProjectEntity();
        project.setName("Webshop Relaunch");
        project.setResponsibleEmployeeId(1L);
        project.setCustomerId(7L);
        project.setStartDate(LocalDate.of(2026, 11, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        project.addEmployee(new ProjectEmployeeEntity(2L, "Developer"));
        Long projectId = projectRepository.save(project).getId();

        // Prüfen, dass die Mitarbeiterzuordnung in der Datenbank existiert.
        Long assignmentCountBefore = entityManager.createQuery(
                        "SELECT COUNT(e) FROM ProjectEmployeeEntity e "
                                + "WHERE e.project.id = :projectId",
                        Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult();

        assertThat(assignmentCountBefore).isEqualTo(1L);

        // Projekt löschen.
        mockMvc.perform(delete("/projects/" + projectId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Prüfen, dass das Projekt gelöscht wurde.
        assertThat(projectRepository.findById(projectId)).isEmpty();

        // Prüfen, dass auch die Mitarbeiterzuordnung aus der Datenbank gelöscht wurde.
        Long assignmentCountAfter = entityManager.createQuery(
                        "SELECT COUNT(e) FROM ProjectEmployeeEntity e "
                                + "WHERE e.project.id = :projectId",
                        Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult();

        assertThat(assignmentCountAfter).isZero();
    }

    @Test
    @WithMockUser
    void deleteUnknownProjectReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/projects/999999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Nicht gefunden"));
    }

    @Test
    void deleteWithoutAuthenticationIsRejectedAndProjectStillExists() throws Exception {
        // Projekt direkt in der Datenbank anlegen.
        ProjectEntity project = new ProjectEntity();
        project.setName("Webshop Relaunch");
        project.setResponsibleEmployeeId(1L);
        project.setCustomerId(7L);
        project.setStartDate(LocalDate.of(2026, 11, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        project = projectRepository.save(project);

        Long projectId = project.getId();

        mockMvc.perform(delete("/projects/" + projectId)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        // Ohne Authentifizierung darf das Projekt nicht gelöscht werden.
        assertThat(projectRepository.findById(projectId)).isPresent();
    }
}
