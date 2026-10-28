package de.szut.pms.project;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

/**
 * Integrationstests für die Story "Mitarbeiter aus einem Projekt entfernen"
 * (DELETE /projects/{projectId}/employees/{employeeId}).
 */
class RemoveEmployeeTest extends AbstractIntegrationTest {

    /** Speichert ein Projekt, in dem die Mitarbeiter 1 (Java) und 2 (Angular) eingeplant sind. */
    private ProjectEntity saveProjectWithTwoEmployees() {
        ProjectEntity project = new ProjectEntity();
        project.setName("Webshop");
        project.setResponsibleEmployeeId(5L);
        project.setCustomerId(7L);
        project.setStartDate(LocalDate.of(2026, 11, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        project.addEmployee(new ProjectEmployeeEntity(1L, "Java"));
        project.addEmployee(new ProjectEmployeeEntity(2L, "Angular"));
        return projectRepository.save(project);
    }

    @Test
    @WithMockUser
    void removeEmployeeRemovesOnlyThisEmployee() throws Exception {
        ProjectEntity project = saveProjectWithTwoEmployees();

        mockMvc.perform(delete("/projects/" + project.getId() + "/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(project.getId()))
                .andExpect(jsonPath("employees", hasSize(1)))
                .andExpect(jsonPath("employees[0].employeeId").value(2));

        // Ist die Zuordnung wirklich aus der Datenbank gelöscht?
        mockMvc.perform(get("/projects/" + project.getId()))
                .andExpect(jsonPath("employees", hasSize(1)))
                .andExpect(jsonPath("employees[0].employeeId").value(2));
    }

    @Test
    void removeEmployeeWithoutAuthenticationIsRejected() throws Exception {
        ProjectEntity project = saveProjectWithTwoEmployees();

        mockMvc.perform(delete("/projects/" + project.getId() + "/employees/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void removeEmployeeFromUnknownProjectReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/projects/9999/employees/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Nicht gefunden"));
    }

    @Test
    @WithMockUser
    void removeEmployeeWhoIsNotInTheProjectReturnsNotFound() throws Exception {
        ProjectEntity project = saveProjectWithTwoEmployees();

        mockMvc.perform(delete("/projects/" + project.getId() + "/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Mitarbeiter nicht im Projekt"));
    }
}
