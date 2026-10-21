package de.szut.pms.employee;

import java.util.List;

/**
 * Antwort des Employee-Service auf GET /employees/{id}/qualifications, z. B.:
 * {"id": 1, "lastName": "Mustermann", "firstName": "Max", "skillSet": [{"skill": "Java", "id": 1}]}
 */
public record EmployeeQualificationsDto(Long id, String lastName, String firstName, List<QualificationDto> skillSet) {
}
