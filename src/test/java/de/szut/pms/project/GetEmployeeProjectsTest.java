package de.szut.pms.project;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

/**
 * Integrationstests für die Story "Projekte eines Mitarbeiters abrufen"
 * (GET /employees/{employeeId}/projects).
 */
class GetEmployeeProjectsTest extends AbstractIntegrationTest {

    private ProjectEntity newProject(String name, LocalDate start, LocalDate end) {
        ProjectEntity project = new ProjectEntity();
        project.setName(name);
        project.setResponsibleEmployeeId(5L);
        project.setCustomerId(7L);
        project.setStartDate(start);
        project.setEndDate(end);
        return project;
    }

    @Test
    @WithMockUser
    void getProjectsReturnsAllProjectsOfTheEmployeeWithQualification() throws Exception {
        // Mitarbeiter 1: in zwei Projekten (Java, dann Angular)
        ProjectEntity later = newProject("Intranet", LocalDate.of(2027, 1, 1), LocalDate.of(2027, 3, 31));
        later.addEmployee(new ProjectEmployeeEntity(1L, "Angular"));
        projectRepository.save(later);

        ProjectEntity earlier = newProject("Webshop", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31));
        earlier.addEmployee(new ProjectEmployeeEntity(1L, "Java"));
        earlier.addEmployee(new ProjectEmployeeEntity(2L, "Java"));
        earlier = projectRepository.save(earlier);

        // Projekt nur mit Mitarbeiter 2 – darf in der Antwort nicht auftauchen
        ProjectEntity other = newProject("Buchhaltung", LocalDate.of(2027, 5, 1), LocalDate.of(2027, 6, 30));
        other.addEmployee(new ProjectEmployeeEntity(2L, "SQL"));
        projectRepository.save(other);

        mockMvc.perform(get("/employees/1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("employeeId").value(1))
                .andExpect(jsonPath("projects", hasSize(2)))
                // nach Startdatum sortiert: zuerst Webshop, dann Intranet
                .andExpect(jsonPath("projects[0].projectId").value(earlier.getId()))
                .andExpect(jsonPath("projects[0].projectName").value("Webshop"))
                .andExpect(jsonPath("projects[0].startDate").value("2026-11-01"))
                .andExpect(jsonPath("projects[0].endDate").value("2026-12-31"))
                .andExpect(jsonPath("projects[0].qualification").value("Java"))
                .andExpect(jsonPath("projects[1].projectName").value("Intranet"))
                .andExpect(jsonPath("projects[1].qualification").value("Angular"));
    }

    @Test
    @WithMockUser
    void getProjectsOfEmployeeWithoutProjectsReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/employees/3/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("employeeId").value(3))
                .andExpect(jsonPath("projects", hasSize(0)));
    }

    @Test
    @WithMockUser
    void getProjectsOfUnknownEmployeeReturnsNotFound() throws Exception {
        when(employeeClient.employeeExists(99L)).thenReturn(false);

        mockMvc.perform(get("/employees/99/projects"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Mitarbeiter nicht gefunden"));
    }

    @Test
    void getProjectsWithoutAuthenticationIsRejected() throws Exception {
        mockMvc.perform(get("/employees/1/projects"))
                .andExpect(status().isUnauthorized());
    }
}