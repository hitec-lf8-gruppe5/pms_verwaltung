package de.szut.pms.project;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeNotFoundException;
import de.szut.pms.employee.QualificationNotFoundException;

/**
 * Fachlogik für die Mitarbeiter eines Projekts (zuweisen, später auch entfernen).
 */
@Service
public class ProjectEmployeeService {

    private final ProjectRepository repository;
    private final ProjectMapper mapper;
    private final EmployeeClient employeeClient;

    public ProjectEmployeeService(ProjectRepository repository,
                                  ProjectMapper mapper,
                                  EmployeeClient employeeClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.employeeClient = employeeClient;
    }

    /**
     * Weist einem Projekt einen Mitarbeiter mit einer Qualifikation zu.
     *
     * @throws ProjectNotFoundException         wenn es das Projekt nicht gibt (404)
     * @throws EmployeeNotFoundException        wenn es den Mitarbeiter nicht gibt (404)
     * @throws QualificationNotFoundException   wenn der Mitarbeiter die Qualifikation nicht hat (422)
     * @throws EmployeeAlreadyAssignedException wenn der Mitarbeiter schon im Projekt ist (409)
     * @throws EmployeeAlreadyBookedException   wenn der Mitarbeiter im selben Zeitraum schon
     *                                          in einem anderen Projekt ist (409)
     */
    @Transactional
    public ProjectResponseDto assignEmployee(Long projectId, AssignEmployeeDto dto) {
        Long employeeId = dto.employeeId();
        String qualification = dto.qualification();

        // 1. Gibt es das Projekt?
        ProjectEntity project = repository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        // 2. Gibt es den Mitarbeiter im Employee-Service?
        if (!employeeClient.employeeExists(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }

        // 3. Hat der Mitarbeiter diese Qualifikation?
        List<String> qualifications = employeeClient.getQualifications(employeeId);
        if (!qualifications.contains(qualification)) {
            throw new QualificationNotFoundException(employeeId, qualification);
        }

        // 4. Ist der Mitarbeiter schon in diesem Projekt?
        for (ProjectEmployeeEntity projectEmployee : project.getEmployees()) {
            if (projectEmployee.getEmployeeId().equals(employeeId)) {
                throw new EmployeeAlreadyAssignedException(projectId, employeeId);
            }
        }

        // 5. Ist der Mitarbeiter im selben Zeitraum schon in einem anderen Projekt eingeplant?
        List<ProjectEntity> overlappingProjects = repository.findOverlappingProjects(
                employeeId, projectId, project.getStartDate(), project.getEndDate());
        if (!overlappingProjects.isEmpty()) {
            throw new EmployeeAlreadyBookedException(employeeId, overlappingProjects.get(0));
        }

        // 6. Alles in Ordnung → zuweisen und speichern
        project.addEmployee(new ProjectEmployeeEntity(employeeId, qualification));
        ProjectEntity saved = repository.save(project);
        return mapper.toDto(saved);
    }
}
