package de.szut.pms.project;

/** Wird geworfen, wenn ein Mitarbeiter gar nicht im Projekt ist (z. B. beim Entfernen) → HTTP 404. */
public class EmployeeNotInProjectException extends RuntimeException {

    public EmployeeNotInProjectException(Long projectId, Long employeeId) {
        super("Der Mitarbeiter mit der Id " + employeeId + " ist dem Projekt " + projectId + " nicht zugeordnet.");
    }
}
