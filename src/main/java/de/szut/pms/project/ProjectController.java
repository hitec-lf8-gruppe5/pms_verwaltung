package de.szut.pms.project;

import java.net.URI;
import java.util.List;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST-Endpunkte für Projekte. Kümmert sich nur um HTTP (URL, Statuscode, JSON);
 * die Fachlogik steckt im {@link ProjectService}.
 */
@RestController
@RequestMapping("/projects")
@Tag(name = "Projekte", description = "Projekte anlegen, abrufen und ändern")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    /** Legt ein Projekt an. Antwort: 201 Created + Location-Header + das angelegte Projekt. */
    @Operation(summary = "Projekt anlegen",
            description = "Legt ein neues Projekt an. Der verantwortliche Mitarbeiter wird gegen den "
                    + "Employee-Service geprüft, die Kunden-Id gegen den Kunden-Service.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projekt angelegt; Location-Header zeigt auf das neue Projekt"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (Pflichtfeld fehlt, Enddatum vor Startdatum …)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Kein oder ungültiger JWT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Verantwortlicher Mitarbeiter oder Kunde unbekannt",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<ProjectResponseDto> create(@RequestBody @Valid ProjectRequestDto dto) {
        ProjectResponseDto created = service.create(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /** Alle Projekte. Antwort: 200 + Liste (leer, wenn es keine gibt). */
    @Operation(summary = "Alle Projekte abrufen",
            description = "Liefert alle Projekte mit allen Daten inkl. Mitarbeitern, sortiert nach Id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste der Projekte (leer, wenn es keine gibt)"),
            @ApiResponse(responseCode = "401", description = "Kein oder ungültiger JWT", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /** Ein Projekt über seine Id. Antwort: 200 oder 404. */
    @Operation(summary = "Ein Projekt abrufen", description = "Liefert ein Projekt mit allen Daten inkl. Mitarbeitern.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projekt gefunden"),
            @ApiResponse(responseCode = "401", description = "Kein oder ungültiger JWT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Kein Projekt mit dieser Id",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> findById(
            @Parameter(description = "Id des Projekts", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /** Ändert alle Daten eines Projekts. Antwort: 200 + das geänderte Projekt. */
    @Operation(summary = "Projektdaten ändern",
            description = "Überschreibt alle Daten eines Projekts. Id und zugeordnete Mitarbeiter bleiben erhalten. "
                    + "Es gelten dieselben Prüfungen wie beim Anlegen.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projekt geändert"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (Pflichtfeld fehlt, Enddatum vor Startdatum …)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Kein oder ungültiger JWT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Projekt, verantwortlicher Mitarbeiter oder Kunde unbekannt",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> update(
            @Parameter(description = "Id des Projekts", example = "1") @PathVariable Long id,
            @RequestBody @Valid ProjectRequestDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
}
