package de.szut.pms.project;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

/** Integrationstests für die Story "Projekte abrufen" (GET /projects, GET /projects/{id}). */
class GetProjectsTest extends AbstractIntegrationTest {

    /** Legt ein Projekt direkt über das Repository an – ohne den POST-Endpunkt. */
    private ProjectEntity saveProject(String name) {
        ProjectEntity project = new ProjectEntity();
        project.setName(name);
        project.setResponsibleEmployeeId(1L);
        project.setCustomerId(7L);
        project.setCustomerContactName("Erika Musterfrau");
        project.setComment("Testprojekt");
        project.setStartDate(LocalDate.of(2026, 11, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        return projectRepository.save(project);
    }

    @Test
    @WithMockUser
    void findAllReturnsEmptyListWhenThereAreNoProjects() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    void findAllReturnsAllProjectsWithTheirData() throws Exception {
        ProjectEntity project = new ProjectEntity();
        project.setName("Webshop");
        project.setResponsibleEmployeeId(1L);
        project.setCustomerId(7L);
        project.setStartDate(LocalDate.of(2026, 11, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        project.addEmployee(new ProjectEmployeeEntity(3L, "Java"));
        projectRepository.save(project);
        saveProject("Intranet");

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Webshop"))
                .andExpect(jsonPath("$[0].employees", hasSize(1)))
                .andExpect(jsonPath("$[0].employees[0].employeeId").value(3))
                .andExpect(jsonPath("$[0].employees[0].qualification").value("Java"))
                .andExpect(jsonPath("$[1].name").value("Intranet"));
    }

    @Test
    @WithMockUser
    void findByIdReturnsTheProject() throws Exception {
        ProjectEntity saved = saveProject("Webshop");

        mockMvc.perform(get("/projects/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(saved.getId()))
                .andExpect(jsonPath("name").value("Webshop"))
                .andExpect(jsonPath("responsibleEmployeeId").value(1))
                .andExpect(jsonPath("customerId").value(7))
                .andExpect(jsonPath("customerContactName").value("Erika Musterfrau"))
                .andExpect(jsonPath("comment").value("Testprojekt"))
                .andExpect(jsonPath("startDate").value("2026-11-01"))
                .andExpect(jsonPath("endDate").value("2026-12-31"))
                .andExpect(jsonPath("employees", hasSize(0)));
    }

    @Test
    @WithMockUser
    void findByIdWithUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/projects/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Nicht gefunden"));
    }

    @Test
    void findAllWithoutAuthenticationIsRejected() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isUnauthorized());
    }
}
