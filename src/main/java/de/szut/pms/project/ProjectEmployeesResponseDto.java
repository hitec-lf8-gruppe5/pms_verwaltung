package de.szut.pms.project;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/** Antwort auf GET /projects/{projectId}/employees: Projekt-Id, Bezeichnung und die Mitarbeiter. */
public record ProjectEmployeesResponseDto(
        @Schema(description = "Id des Projekts", example = "1")
        Long projectId,
        @Schema(description = "Bezeichnung des Projekts", example = "Webshop Relaunch")
        String projectName,
        @Schema(description = "Mitarbeiter im Projekt mit ihrer Qualifikation")
        List<ProjectEmployeeDto> employees) {
}