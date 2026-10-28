package de.szut.pms.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Testet den EmployeeServiceClient ohne echten Employee-Service:
 * MockRestServiceServer spielt den Employee-Service und liefert vorgegebene Antworten.
 */
class EmployeeServiceClientTest {

    private MockRestServiceServer employeeService;
    private EmployeeServiceClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        employeeService = MockRestServiceServer.bindTo(builder).build();
        client = new EmployeeServiceClient(builder, "http://localhost:8089");

        // So tun, als hätte der User unsere Anfrage mit dem Token "test-token" geschickt
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("sub", "john")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void employeeExists_returnsTrue_whenEmployeeServiceAnswers200() {
        employeeService.expect(requestTo("http://localhost:8089/employees/1"))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess("{\"id\": 1}", MediaType.APPLICATION_JSON));

        assertTrue(client.employeeExists(1L));
        employeeService.verify();
    }

    @Test
    void employeeExists_returnsFalse_whenEmployeeServiceAnswers404() {
        employeeService.expect(requestTo("http://localhost:8089/employees/99"))
                .andRespond(withResourceNotFound());

        assertFalse(client.employeeExists(99L));
        employeeService.verify();
    }

    @Test
    void employeeExists_sendsNoAuthorizationHeader_whenThereIsNoJwt() {
        SecurityContextHolder.clearContext();
        employeeService.expect(requestTo("http://localhost:8089/employees/1"))
                .andExpect(headerDoesNotExist("Authorization"))
                .andRespond(withSuccess("{\"id\": 1}", MediaType.APPLICATION_JSON));

        assertTrue(client.employeeExists(1L));
        employeeService.verify();
    }

    @Test
    void getQualifications_returnsSkills() {
        String json = """
                {
                  "id": 2,
                  "lastName": "Musterfrau",
                  "firstName": "Susanne",
                  "skillSet": [
                    {"skill": "Angular", "id": 2},
                    {"skill": "Java", "id": 1}
                  ]
                }
                """;
        employeeService.expect(requestTo("http://localhost:8089/employees/2/qualifications"))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<String> qualifications = client.getQualifications(2L);

        assertEquals(List.of("Angular", "Java"), qualifications);
        employeeService.verify();
    }

    @Test
    void getQualifications_returnsEmptyList_whenEmployeeNotFound() {
        employeeService.expect(requestTo("http://localhost:8089/employees/99/qualifications"))
                .andRespond(withResourceNotFound());

        List<String> qualifications = client.getQualifications(99L);

        assertTrue(qualifications.isEmpty());
        employeeService.verify();
    }
}
