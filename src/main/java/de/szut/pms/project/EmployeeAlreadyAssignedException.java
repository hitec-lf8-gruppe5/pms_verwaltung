package de.szut.pms.project;

/** Wird geworfen, wenn ein Mitarbeiter dem Projekt schon zugeordnet ist → HTTP 409. */
public class EmployeeAlreadyAssignedException extends RuntimeException {

    public EmployeeAlreadyAssignedException(Long projectId, Long employeeId) {
        super("Der Mitarbeiter mit der Id " + employeeId + " ist dem Projekt " + projectId + " bereits zugeordnet.");
    }
}
