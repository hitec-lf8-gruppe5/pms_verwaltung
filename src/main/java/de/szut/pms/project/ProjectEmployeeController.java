package de.szut.pms.project;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST-Endpunkte für die Mitarbeiter eines Projekts.
 * Kümmert sich nur um HTTP; die Fachlogik steckt im {@link ProjectEmployeeService}.
 */
@RestController
@RequestMapping("/projects/{projectId}/employees")
@Tag(name = "Projektmitarbeiter", description = "Mitarbeiter einem Projekt zuweisen und daraus entfernen")
public class ProjectEmployeeController {

    private final ProjectEmployeeService service;

    public ProjectEmployeeController(ProjectEmployeeService service) {
        this.service = service;
    }

    /** Weist einen Mitarbeiter zu. Antwort: 201 Created + das aktualisierte Projekt. */
    @Operation(summary = "Mitarbeiter einem Projekt zuweisen",
            description = "Weist einem Projekt einen Mitarbeiter mit einer Qualifikation zu. Mitarbeiter und "
                    + "Qualifikation werden gegen den Employee-Service geprüft. Ein Mitarbeiter darf im selben "
                    + "Zeitraum nur in einem Projekt sein.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mitarbeiter zugewiesen; Antwort enthält das aktualisierte Projekt"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (Mitarbeiter-Id oder Qualifikation fehlt)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Kein oder ungültiger JWT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Projekt oder Mitarbeiter unbekannt",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Mitarbeiter ist schon im Projekt oder im selben Zeitraum "
                    + "in einem anderen Projekt eingeplant",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Mitarbeiter hat die Qualifikation nicht",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<ProjectResponseDto> assignEmployee(
            @Parameter(description = "Id des Projekts", example = "1") @PathVariable Long projectId,
            @RequestBody @Valid AssignEmployeeDto dto) {
        ProjectResponseDto project = service.assignEmployee(projectId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    /** Entfernt einen Mitarbeiter aus dem Projekt. Antwort: 200 + das aktualisierte Projekt. */
    @Operation(summary = "Mitarbeiter aus einem Projekt entfernen",
            description = "Entfernt die Zuordnung eines Mitarbeiters zum Projekt. "
                    + "Danach ist der Mitarbeiter in diesem Zeitraum wieder frei für andere Projekte.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mitarbeiter entfernt; Antwort enthält das aktualisierte Projekt"),
            @ApiResponse(responseCode = "401", description = "Kein oder ungültiger JWT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Projekt unbekannt oder Mitarbeiter nicht im Projekt",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ProjectResponseDto> removeEmployee(
            @Parameter(description = "Id des Projekts", example = "1") @PathVariable Long projectId,
            @Parameter(description = "Id des Mitarbeiters", example = "1") @PathVariable Long employeeId) {
        ProjectResponseDto project = service.removeEmployee(projectId, employeeId);
        return ResponseEntity.ok(project);
    }
}
