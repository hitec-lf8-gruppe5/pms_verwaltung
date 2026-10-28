package de.szut.pms.testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.szut.pms.customer.CustomerClient;
import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.project.ProjectRepository;

/**
 * Startet einen echten Postgres-Container über Testcontainers und die volle
 * Anwendung. Zum Authentifizieren in Tests nicht Authentik ansprechen —
 * dafür gibt es {@code @WithMockUser}.
 * <p>
 * Employee- und Kunden-Service werden in allen Tests durch Mocks ersetzt:
 * Wir bestimmen selbst, was sie antworten. Standard: alles existiert.
 * Negativtests überschreiben das, z. B.
 * {@code when(employeeClient.employeeExists(99L)).thenReturn(false);}
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = PostgresContextInitializer.class)
public class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ProjectRepository projectRepository;

    @MockitoBean
    protected EmployeeClient employeeClient;

    @MockitoBean
    protected CustomerClient customerClient;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        when(employeeClient.employeeExists(any())).thenReturn(true);
        when(customerClient.existsById(any())).thenReturn(true);
    }
}
