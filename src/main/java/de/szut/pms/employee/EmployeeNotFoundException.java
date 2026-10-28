package de.szut.pms.employee;

/** Wird geworfen, wenn es im Employee-Service keinen Mitarbeiter mit dieser Id gibt → HTTP 404. */
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long id) {
        super("Es gibt keinen Mitarbeiter mit der Id " + id + ".");
    }
}
