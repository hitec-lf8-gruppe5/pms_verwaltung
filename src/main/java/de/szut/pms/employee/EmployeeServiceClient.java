package de.szut.pms.employee;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Fragt den echten Employee-Service per HTTP ab.
 * <p>
 * Der Employee-Service ist mit JWT abgesichert. Deshalb schicken wir bei jeder
 * Anfrage den Token mit, mit dem der User unsere eigene Anfrage geschickt hat
 * (Token Relay, siehe AUTHENTIK.md).
 */
@Component
public class EmployeeServiceClient implements EmployeeClient {

    private final RestClient restClient;

    public EmployeeServiceClient(RestClient.Builder restClientBuilder,
                                 @Value("${employee-service.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public boolean employeeExists(Long employeeId) {
        try {
            restClient.get()
                    .uri("/employees/{id}", employeeId)
                    .header("Authorization", getBearerToken())
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @Override
    public List<String> getQualifications(Long employeeId) {
        EmployeeQualificationsDto response;
        try {
            response = restClient.get()
                    .uri("/employees/{id}/qualifications", employeeId)
                    .header("Authorization", getBearerToken())
                    .retrieve()
                    .body(EmployeeQualificationsDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            return new ArrayList<>();
        }

        List<String> qualifications = new ArrayList<>();
        if (response == null || response.skillSet() == null) {
            return qualifications;
        }
        for (QualificationDto qualification : response.skillSet()) {
            qualifications.add(qualification.skill());
        }
        return qualifications;
    }

    /** Holt den JWT aus der aktuellen Anfrage und baut daraus "Bearer <token>". */
    private String getBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return "Bearer " + jwtAuthentication.getToken().getTokenValue();
        }
        return "";
    }
}
