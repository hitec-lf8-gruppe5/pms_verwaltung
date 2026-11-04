package de.szut.pms.project;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

/** Ein Projekt aus Sicht eines Mitarbeiters: Projektdaten und seine Qualifikation darin. */
public record EmployeeProjectDto(
        @Schema(description = "Id des Projekts", example = "1")
        Long projectId,
        @Schema(description = "Bezeichnung des Projekts", example = "Webshop Relaunch")
        String projectName,
        @Schema(description = "Startdatum", example = "2026-11-01")
        LocalDate startDate,
        @Schema(description = "Enddatum", example = "2026-12-31")
        LocalDate endDate,
        @Schema(description = "Qualifikation, mit der der Mitarbeiter im Projekt eingesetzt ist", example = "Java")
        String qualification) {
}