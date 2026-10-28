package de.szut.pms.employee;

/** Wird geworfen, wenn ein Mitarbeiter die gewünschte Qualifikation nicht hat → HTTP 422. */
public class QualificationNotFoundException extends RuntimeException {

    public QualificationNotFoundException(Long employeeId, String qualification) {
        super("Der Mitarbeiter mit der Id " + employeeId + " hat die Qualifikation '" + qualification + "' nicht.");
    }
}
