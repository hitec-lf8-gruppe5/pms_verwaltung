package de.szut.pms.project;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/** Was der Service über ein Projekt zurückgibt: alle Daten inkl. der Mitarbeiter. */
public record ProjectResponseDto(
        @Schema(description = "Id des Projekts", example = "1")
        Long id,
        @Schema(description = "Bezeichnung des Projekts", example = "Webshop Relaunch")
        String name,
        @Schema(description = "Id des verantwortlichen Mitarbeiters", example = "1")
        Long responsibleEmployeeId,
        @Schema(description = "Id des Kunden", example = "7")
        Long customerId,
        @Schema(description = "Name der zuständigen Person beim Kunden", example = "Erika Musterfrau")
        String customerContactName,
        @Schema(description = "Kommentar zum Projektziel", example = "Neuer Webshop mit Online-Bezahlung")
        String comment,
        @Schema(description = "Startdatum", example = "2026-11-01")
        LocalDate startDate,
        @Schema(description = "Enddatum", example = "2026-12-31")
        LocalDate endDate,
        @Schema(description = "Mitarbeiter im Projekt mit ihrer Qualifikation")
        List<ProjectEmployeeDto> employees) {
}
