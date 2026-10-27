package de.szut.pms.project;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

/**
 * REST-Endpunkte für Projekte. Kümmert sich nur um HTTP (URL, Statuscode, JSON);
 * die Fachlogik steckt im {@link ProjectService}.
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    /** Legt ein Projekt an. Antwort: 201 Created + Location-Header + das angelegte Projekt. */
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
    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /** Ein Projekt über seine Id. Antwort: 200 oder 404. */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
