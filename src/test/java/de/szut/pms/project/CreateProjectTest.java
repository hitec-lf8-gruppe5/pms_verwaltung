package de.szut.pms.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

/** Integrationstests für die Story "Projekte anlegen" (POST /projects). */
class CreateProjectTest extends AbstractIntegrationTest {

    private static final String VALID_PROJECT = """
            {
                "name": "Webshop Relaunch",
                "responsibleEmployeeId": 1,
                "customerId": 7,
                "customerContactName": "Erika Musterfrau",
                "comment": "Neuer Webshop für den Kunden",
                "startDate": "2026-11-01",
                "endDate": "2026-12-31"
            }
            """;

    @Test
    @WithMockUser
    void createStoresAndReturnsTheProject() throws Exception {
        mockMvc.perform(post("/projects")
                        .content(VALID_PROJECT).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value("Webshop Relaunch"))
                .andExpect(jsonPath("responsibleEmployeeId").value(1))
                .andExpect(jsonPath("customerId").value(7))
                .andExpect(jsonPath("startDate").value("2026-11-01"))
                .andExpect(jsonPath("endDate").value("2026-12-31"))
                .andExpect(jsonPath("employees").isEmpty());

        List<ProjectEntity> stored = projectRepository.findAll();
        assertThat(stored).hasSize(1);
        assertThat(stored.get(0).getName()).isEqualTo("Webshop Relaunch");
        assertThat(stored.get(0).getStartDate()).isEqualTo(LocalDate.of(2026, 11, 1));
    }

    @Test
    void createWithoutAuthenticationIsRejected() throws Exception {
        mockMvc.perform(post("/projects")
                        .content(VALID_PROJECT).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        assertThat(projectRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser
    void createWithMissingRequiredFieldsIsRejected() throws Exception {
        final String content = """
                {
                    "comment": "Name, Mitarbeiter, Kunde und Daten fehlen"
                }
                """;

        mockMvc.perform(post("/projects")
                        .content(content).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.name").exists())
                .andExpect(jsonPath("errors.responsibleEmployeeId").exists())
                .andExpect(jsonPath("errors.customerId").exists())
                .andExpect(jsonPath("errors.startDate").exists())
                .andExpect(jsonPath("errors.endDate").exists());

        assertThat(projectRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser
    void createWithEndDateBeforeStartDateIsRejected() throws Exception {
        final String content = """
                {
                    "name": "Webshop Relaunch",
                    "responsibleEmployeeId": 1,
                    "customerId": 7,
                    "startDate": "2026-12-31",
                    "endDate": "2026-11-01"
                }
                """;

        mockMvc.perform(post("/projects")
                        .content(content).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.endDateNotBeforeStartDate").exists());

        assertThat(projectRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser
    void createWithUnknownResponsibleEmployeeIsRejected() throws Exception {
        when(employeeClient.employeeExists(1L)).thenReturn(false);

        mockMvc.perform(post("/projects")
                        .content(VALID_PROJECT).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Mitarbeiter nicht gefunden"));

        assertThat(projectRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser
    void createWithUnknownCustomerIsRejected() throws Exception {
        when(customerClient.existsById(7L)).thenReturn(false);

        mockMvc.perform(post("/projects")
                        .content(VALID_PROJECT).contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("title").value("Kunde nicht gefunden"));

        assertThat(projectRepository.findAll()).isEmpty();
    }
}
