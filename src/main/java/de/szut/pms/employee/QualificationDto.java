package de.szut.pms.employee;

/** Eine Qualifikation, so wie der Employee-Service sie liefert: {"skill": "Java", "id": 1} */
public record QualificationDto(Long id, String skill) {
}
