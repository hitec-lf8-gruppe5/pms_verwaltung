package de.szut.pms.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.support.TransactionTemplate;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

/** Integrationstests für die Story "Projektdaten aktualisieren" (PUT /projects/{id}). */
class UpdateProjectTest extends AbstractIntegrationTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final String UPDATED_PROJECT = """
            {
                "name": "Webshop 2.0",
                "responsibleEmployeeId": 2,
                "customerId": 8,
                "customerContactName": "Max Mustermann",
                "comment": "Ziel erweitert",
                "startDate": "2027-01-01",
                "endDate": "2027-03-31"
            }
            """;

    /** Legt ein Projekt mit einem zugeordneten Mitarbeiter direkt über das Repository an. */
    private ProjectEntity saveProject() {
        ProjectEntity project = new ProjectEntity();
        project.setName("Webshop");
        project.setResponsibleEmployeeId(1L);
        project.setCustomerId(7L);
        project.setCustomerContactName("Erika Musterfrau");
        project.setComment("Alter Kommentar");
        project.setStartDate(LocalDate.of(2026, 11, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        project.addEmployee(new ProjectEmployeeEntity(3L, "Java"));
        return projectRepository.save(project);
    }

    @Test
    @WithMockUser
    void updateChangesAllDataAndKeepsIdAndEmployees() throws Exception {
        ProjectEntity saved = saveProject();

        mockMvc.perform(put("/projects/" + saved.getId())
                        .content(UPDATED_PROJECT).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(saved.getId()))
                .andExpect(jsonPath("name").value("Webshop 2.0"))
                .andExpect(jsonPath("responsibleEmployeeId").value(2))
                .andExpect(jsonPath("customerId").value(8))
                .andExpect(jsonPath("customerContactName").value("Max Mustermann"))
                .andExpect(jsonPath("comment").value("Ziel erweitert"))
                .andExpect(jsonPath("startDate").value("2027-01-01"))
                .andExpect(jsonPath("endDate").value("2027-03-31"))
                .andExpect(jsonPath("employees", hasSize(1)))
                .andExpect(jsonPath("employees[0].employeeId").value(3));

        // Direkt in der Datenbank prüfen (Mitarbeiter werden "lazy" geladen → in einer Transaktion lesen)
        transactionTemplate.executeWithoutResult(status -> {
            ProjectEntity stored = projectRepository.findById(saved.getId()).orElseThrow();
            assertThat(stored.getName()).isEqualTo("Webshop 2.0");
            assertThat(stored.getEndDate()).isEqualTo(LocalDate.of(2027, 3, 31));
            assertThat(stored.getEmployees()).hasSize(1);
        });
        assertThat(projectRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser
    void updateWithUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(put("/projects/9999")
                        .content(UPDATED_PROJECT).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Nicht gefunden"));
    }

    @Test
    @WithMockUser
    void updateWithMissingNameIsRejected() throws Exception {
        ProjectEntity saved = saveProject();
        final String content = UPDATED_PROJECT.replace("\"name\": \"Webshop 2.0\",", "");

        mockMvc.perform(put("/projects/" + saved.getId())
                        .content(content).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.name").exists());

        assertThat(projectRepository.findById(saved.getId()).orElseThrow().getName()).isEqualTo("Webshop");
    }

    @Test
    @WithMockUser
    void updateWithEndDateBeforeStartDateIsRejected() throws Exception {
        ProjectEntity saved = saveProject();
        final String content = UPDATED_PROJECT.replace("\"endDate\": \"2027-03-31\"", "\"endDate\": \"2026-12-01\"");

        mockMvc.perform(put("/projects/" + saved.getId())
                        .content(content).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.endDateNotBeforeStartDate").exists());

        assertThat(projectRepository.findById(saved.getId()).orElseThrow().getName()).isEqualTo("Webshop");
    }

    @Test
    @WithMockUser
    void updateWithUnknownResponsibleEmployeeIsRejected() throws Exception {
        ProjectEntity saved = saveProject();
        when(employeeClient.employeeExists(2L)).thenReturn(false);

        mockMvc.perform(put("/projects/" + saved.getId())
                        .content(UPDATED_PROJECT).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Mitarbeiter nicht gefunden"));

        assertThat(projectRepository.findById(saved.getId()).orElseThrow().getName()).isEqualTo("Webshop");
    }

    @Test
    @WithMockUser
    void updateWithUnknownCustomerIsRejected() throws Exception {
        ProjectEntity saved = saveProject();
        when(customerClient.existsById(8L)).thenReturn(false);

        mockMvc.perform(put("/projects/" + saved.getId())
                        .content(UPDATED_PROJECT).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Kunde nicht gefunden"));

        assertThat(projectRepository.findById(saved.getId()).orElseThrow().getName()).isEqualTo("Webshop");
    }

    @Test
    void updateWithoutAuthenticationIsRejected() throws Exception {
        ProjectEntity saved = saveProject();

        mockMvc.perform(put("/projects/" + saved.getId())
                        .content(UPDATED_PROJECT).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        assertThat(projectRepository.findById(saved.getId()).orElseThrow().getName()).isEqualTo("Webshop");
    }
}
