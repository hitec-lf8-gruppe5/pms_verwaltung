package de.szut.pms.project;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

/**
 * Integrationstests für die Story "Doppelte Verplanung eines Mitarbeiters verhindern".
 * <p>
 * In jedem Test ist Mitarbeiter 1 schon im Projekt "Intranet" eingeplant
 * (01.11.2026 bis 30.11.2026). Dann versuchen wir, ihn einem zweiten Projekt zuzuweisen.
 */
class DoubleBookingTest extends AbstractIntegrationTest {

    private static final String JAVA_EMPLOYEE_1 = """
            {
                "employeeId": 1,
                "qualification": "Java"
            }
            """;

    @BeforeEach
    void employeeOneIsAlreadyInIntranet() {
        when(employeeClient.getQualifications(1L)).thenReturn(List.of("Java"));
        when(employeeClient.getQualifications(2L)).thenReturn(List.of("Java"));

        ProjectEntity intranet = newProject("Intranet", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        intranet.addEmployee(new ProjectEmployeeEntity(1L, "Java"));
        projectRepository.save(intranet);
    }

    /** Legt ein Projekt mit diesem Zeitraum an (noch nicht gespeichert). */
    private ProjectEntity newProject(String name, LocalDate startDate, LocalDate endDate) {
        ProjectEntity project = new ProjectEntity();
        project.setName(name);
        project.setResponsibleEmployeeId(5L);
        project.setCustomerId(7L);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        return project;
    }

    @Test
    @WithMockUser
    void overlappingPeriodIsRejected() throws Exception {
        // Webshop: 15.11. - 31.12. → überschneidet sich mit Intranet (bis 30.11.)
        ProjectEntity webshop = projectRepository.save(
                newProject("Webshop", LocalDate.of(2026, 11, 15), LocalDate.of(2026, 12, 31)));

        mockMvc.perform(post("/projects/" + webshop.getId() + "/employees")
                        .content(JAVA_EMPLOYEE_1).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("title").value("Mitarbeiter bereits verplant"))
                .andExpect(jsonPath("detail").value(containsString("Intranet")));
    }

    @Test
    @WithMockUser
    void sameDayAtTheBorderIsRejected() throws Exception {
        // Webshop startet genau am Tag, an dem Intranet endet (30.11.) → zählt als Überschneidung
        ProjectEntity webshop = projectRepository.save(
                newProject("Webshop", LocalDate.of(2026, 11, 30), LocalDate.of(2026, 12, 31)));

        mockMvc.perform(post("/projects/" + webshop.getId() + "/employees")
                        .content(JAVA_EMPLOYEE_1).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("title").value("Mitarbeiter bereits verplant"));
    }

    @Test
    @WithMockUser
    void periodWithoutOverlapIsAccepted() throws Exception {
        // Webshop startet einen Tag nach dem Ende von Intranet → keine Überschneidung
        ProjectEntity webshop = projectRepository.save(
                newProject("Webshop", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)));

        mockMvc.perform(post("/projects/" + webshop.getId() + "/employees")
                        .content(JAVA_EMPLOYEE_1).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("employees[0].employeeId").value(1));
    }

    @Test
    @WithMockUser
    void otherEmployeeInTheSamePeriodIsAccepted() throws Exception {
        // Gleicher Zeitraum wie Intranet, aber Mitarbeiter 2 ist noch frei
        ProjectEntity webshop = projectRepository.save(
                newProject("Webshop", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)));
        String javaEmployee2 = """
                {
                    "employeeId": 2,
                    "qualification": "Java"
                }
                """;

        mockMvc.perform(post("/projects/" + webshop.getId() + "/employees")
                        .content(javaEmployee2).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("employees[0].employeeId").value(2));
    }
}
