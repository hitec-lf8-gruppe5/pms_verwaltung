package de.szut.pms.project;

import io.swagger.v3.oas.annotations.media.Schema;

/** Ein Mitarbeiter im Projekt: seine Id und die Qualifikation, mit der er eingesetzt ist. */
public record ProjectEmployeeDto(
        @Schema(description = "Id des Mitarbeiters", example = "3")
        Long employeeId,
        @Schema(description = "Qualifikation, mit der er im Projekt eingesetzt ist", example = "Java")
        String qualification) {
}
