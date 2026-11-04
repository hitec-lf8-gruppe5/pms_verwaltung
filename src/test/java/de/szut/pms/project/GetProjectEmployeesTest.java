package de.szut.pms.project;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

/**
 * Integrationstests für die Story "Mitarbeiter eines Projekts abrufen"
 * (GET /projects/{projectId}/employees).
 */
class GetProjectEmployeesTest extends AbstractIntegrationTest {

    /** Speichert ein Projekt ohne Mitarbeiter. */
    private ProjectEntity newProject(String name) {
        ProjectEntity project = new ProjectEntity();
        project.setName(name);
        project.setResponsibleEmployeeId(5L);
        project.setCustomerId(7L);
        project.setStartDate(LocalDate.of(2026, 11, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        return project;
    }

    @Test
    @WithMockUser
    void getEmployeesReturnsProjectIdNameAndAllEmployees() throws Exception {
        ProjectEntity project = newProject("Webshop");
        project.addEmployee(new ProjectEmployeeEntity(1L, "Java"));
        project.addEmployee(new ProjectEmployeeEntity(2L, "Angular"));
        project = projectRepository.save(project);

        mockMvc.perform(get("/projects/" + project.getId() + "/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("projectId").value(project.getId()))
                .andExpect(jsonPath("projectName").value("Webshop"))
                .andExpect(jsonPath("employees", hasSize(2)))
                .andExpect(jsonPath("employees[?(@.employeeId == 1)].qualification").value("Java"))
                .andExpect(jsonPath("employees[?(@.employeeId == 2)].qualification").value("Angular"));
    }

    @Test
    @WithMockUser
    void getEmployeesOfProjectWithoutEmployeesReturnsEmptyList() throws Exception {
        ProjectEntity project = projectRepository.save(newProject("Intranet"));

        mockMvc.perform(get("/projects/" + project.getId() + "/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("projectName").value("Intranet"))
                .andExpect(jsonPath("employees", hasSize(0)));
    }

    @Test
    @WithMockUser
    void getEmployeesOfUnknownProjectReturnsNotFound() throws Exception {
        mockMvc.perform(get("/projects/9999/employees"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Nicht gefunden"));
    }

    @Test
    void getEmployeesWithoutAuthenticationIsRejected() throws Exception {
        ProjectEntity project = projectRepository.save(newProject("Webshop"));

        mockMvc.perform(get("/projects/" + project.getId() + "/employees"))
                .andExpect(status().isUnauthorized());
    }
}