package de.szut.pms.project;

/**
 * Wird geworfen, wenn ein Mitarbeiter im selben Zeitraum schon in einem anderen Projekt
 * eingeplant ist → HTTP 409. Die Meldung nennt das Projekt, mit dem es kollidiert.
 */
public class EmployeeAlreadyBookedException extends RuntimeException {

    public EmployeeAlreadyBookedException(Long employeeId, ProjectEntity otherProject) {
        super("Der Mitarbeiter mit der Id " + employeeId
                + " ist im selben Zeitraum schon im Projekt '" + otherProject.getName()
                + "' (Id " + otherProject.getId() + ") eingeplant.");
    }
}
