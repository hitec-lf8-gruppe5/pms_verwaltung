package de.szut.pms.employee;

import java.util.List;

/**
 * Zugriff auf den Employee-Service (http://localhost:8089).
 * Die echte Implementierung ist {@link EmployeeServiceClient}.
 */
public interface EmployeeClient {

    /** Gibt true zurück, wenn es im Employee-Service einen Mitarbeiter mit dieser Id gibt. */
    boolean employeeExists(Long employeeId);

    /**
     * Gibt die Qualifikationen eines Mitarbeiters zurück, z. B. ["Java", "Angular"].
     * Gibt es den Mitarbeiter nicht, ist die Liste leer.
     */
    List<String> getQualifications(Long employeeId);
}
