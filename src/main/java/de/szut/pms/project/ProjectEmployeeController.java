package de.szut.pms.project;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST-Endpunkte für die Mitarbeiter eines Projekts.
 * Kümmert sich nur um HTTP; die Fachlogik steckt im {@link ProjectEmployeeService}.
 */
@RestController
@RequestMapping("/projects/{projectId}/employees")
public class ProjectEmployeeController {

    private final ProjectEmployeeService service;

    public ProjectEmployeeController(ProjectEmployeeService service) {
        this.service = service;
    }

    /** Weist einen Mitarbeiter zu. Antwort: 201 Created + das aktualisierte Projekt. */
    @PostMapping
    public ResponseEntity<ProjectResponseDto> assignEmployee(@PathVariable Long projectId,
                                                             @RequestBody @Valid AssignEmployeeDto dto) {
        ProjectResponseDto project = service.assignEmployee(projectId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }
}
