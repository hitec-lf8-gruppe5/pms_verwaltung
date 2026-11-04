package de.szut.pms.project;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/** Antwort auf GET /employees/{employeeId}/projects: einmalig die Mitarbeiter-Id und seine Projekte. */
public record EmployeeProjectsResponseDto(
        @Schema(description = "Id des Mitarbeiters", example = "3")
        Long employeeId,
        @Schema(description = "Projekte, an denen der Mitarbeiter mitwirkt oder mitgewirkt hat")
        List<EmployeeProjectDto> projects) {
}