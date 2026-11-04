package de.szut.pms.project;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST-Endpunkt für die Projekte eines Mitarbeiters.
 * Kümmert sich nur um HTTP; die Fachlogik steckt im {@link ProjectEmployeeService}.
 */
@RestController
@RequestMapping("/employees/{employeeId}/projects")
@Tag(name = "Mitarbeiterprojekte", description = "Projekte eines Mitarbeiters abrufen")
public class EmployeeProjectController {

    private final ProjectEmployeeService service;

    public EmployeeProjectController(ProjectEmployeeService service) {
        this.service = service;
    }

    /** Alle Projekte eines Mitarbeiters. Antwort: 200 (Liste ggf. leer) oder 404. */
    @Operation(summary = "Projekte eines Mitarbeiters abrufen",
            description = "Liefert einmalig die Mitarbeiter-Id und alle Projekte, an denen der Mitarbeiter "
                    + "mitwirkt oder mitgewirkt hat, jeweils mit Id, Bezeichnung, Start- und Enddatum und "
                    + "seiner Qualifikation im Projekt. Der Mitarbeiter wird gegen den Employee-Service geprüft.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projekte des Mitarbeiters (leer, wenn er keinem Projekt zugeordnet ist)"),
            @ApiResponse(responseCode = "401", description = "Kein oder ungültiger JWT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Mitarbeiter im Employee-Service unbekannt",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<EmployeeProjectsResponseDto> getProjectsOfEmployee(
            @Parameter(description = "Id des Mitarbeiters", example = "3") @PathVariable Long employeeId) {
        return ResponseEntity.ok(service.getProjectsOfEmployee(employeeId));
    }
}