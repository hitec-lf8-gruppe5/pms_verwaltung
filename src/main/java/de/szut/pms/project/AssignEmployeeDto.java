package de.szut.pms.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Was der Client schickt, um einen Mitarbeiter einem Projekt zuzuweisen, z. B.:
 * {"employeeId": 1, "qualification": "Java"}
 */
public record AssignEmployeeDto(

        @NotNull(message = "Die Mitarbeiter-Id muss angegeben werden.")
        @Positive(message = "Die Mitarbeiter-Id muss positiv sein.")
        Long employeeId,

        @NotBlank(message = "Die Qualifikation darf nicht leer sein.")
        String qualification) {
}
