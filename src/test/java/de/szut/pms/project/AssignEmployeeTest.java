package de.szut.pms.project;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

/** Integrationstests für die Story "Mitarbeiter einem Projekt zuweisen" (POST /projects/{id}/employees). */
class AssignEmployeeTest extends AbstractIntegrationTest {

    private static final String JAVA_EMPLOYEE = """
            {
                "employeeId": 1,
                "qualification": "Java"
            }
            """;

    /** Legt ein Projekt direkt über das Repository an – ohne den POST-Endpunkt. */
    private ProjectEntity saveProject() {
        ProjectEntity project = new ProjectEntity();
        project.setName("Webshop");
        project.setResponsibleEmployeeId(5L);
        project.setCustomerId(7L);
        project.setStartDate(LocalDate.of(2026, 11, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        return projectRepository.save(project);
    }

    @Test
    @WithMockUser
    void assignEmployeeAddsTheEmployeeToTheProject() throws Exception {
        ProjectEntity project = saveProject();
        when(employeeClient.getQualifications(1L)).thenReturn(List.of("Java", "Angular"));

        mockMvc.perform(post("/projects/" + project.getId() + "/employees")
                        .content(JAVA_EMPLOYEE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(project.getId()))
                .andExpect(jsonPath("employees", hasSize(1)))
                .andExpect(jsonPath("employees[0].employeeId").value(1))
                .andExpect(jsonPath("employees[0].qualification").value("Java"));

        // Ist die Zuordnung wirklich in der Datenbank gespeichert?
        mockMvc.perform(get("/projects/" + project.getId()))
                .andExpect(jsonPath("employees", hasSize(1)))
                .andExpect(jsonPath("employees[0].employeeId").value(1));
    }

    @Test
    void assignEmployeeWithoutAuthenticationIsRejected() throws Exception {
        ProjectEntity project = saveProject();

        mockMvc.perform(post("/projects/" + project.getId() + "/employees")
                        .content(JAVA_EMPLOYEE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void assignEmployeeWithMissingFieldsIsRejected() throws Exception {
        ProjectEntity project = saveProject();

        mockMvc.perform(post("/projects/" + project.getId() + "/employees")
                        .content("{}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.employeeId").exists())
                .andExpect(jsonPath("errors.qualification").exists());
    }

    @Test
    @WithMockUser
    void assignEmployeeToUnknownProjectReturnsNotFound() throws Exception {
        mockMvc.perform(post("/projects/9999/employees")
                        .content(JAVA_EMPLOYEE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Nicht gefunden"));
    }

    @Test
    @WithMockUser
    void assignUnknownEmployeeReturnsNotFound() throws Exception {
        ProjectEntity project = saveProject();
        when(employeeClient.employeeExists(1L)).thenReturn(false);

        mockMvc.perform(post("/projects/" + project.getId() + "/employees")
                        .content(JAVA_EMPLOYEE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Mitarbeiter nicht gefunden"));
    }

    @Test
    @WithMockUser
    void assignEmployeeWithoutTheQualificationIsRejected() throws Exception {
        ProjectEntity project = saveProject();
        when(employeeClient.getQualifications(1L)).thenReturn(List.of("Angular"));

        mockMvc.perform(post("/projects/" + project.getId() + "/employees")
                        .content(JAVA_EMPLOYEE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("title").value("Qualifikation fehlt"));
    }

    @Test
    @WithMockUser
    void assignEmployeeTwiceIsRejected() throws Exception {
        ProjectEntity project = new ProjectEntity();
        project.setName("Webshop");
        project.setResponsibleEmployeeId(5L);
        project.setCustomerId(7L);
        project.setStartDate(LocalDate.of(2026, 11, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        project.addEmployee(new ProjectEmployeeEntity(1L, "Java"));
        project = projectRepository.save(project);
        when(employeeClient.getQualifications(1L)).thenReturn(List.of("Java"));

        mockMvc.perform(post("/projects/" + project.getId() + "/employees")
                        .content(JAVA_EMPLOYEE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("title").value("Mitarbeiter bereits zugeordnet"));
    }
}
