package de.szut.pms.project;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Was der Client schickt, um einen Mitarbeiter einem Projekt zuzuweisen, z. B.:
 * {"employeeId": 1, "qualification": "Java"}
 */
public record AssignEmployeeDto(

        @Schema(description = "Id des Mitarbeiters (Employee-Service)", example = "1")
        @NotNull(message = "Die Mitarbeiter-Id muss angegeben werden.")
        @Positive(message = "Die Mitarbeiter-Id muss positiv sein.")
        Long employeeId,

        @Schema(description = "Qualifikation, mit der der Mitarbeiter im Projekt eingesetzt wird", example = "Java")
        @NotBlank(message = "Die Qualifikation darf nicht leer sein.")
        String qualification) {
}
