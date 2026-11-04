package de.szut.pms.project;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeNotFoundException;
import de.szut.pms.employee.QualificationNotFoundException;

/**
 * Fachlogik für die Mitarbeiter eines Projekts (zuweisen, entfernen, abfragen).
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

    /**
     * Entfernt einen Mitarbeiter aus einem Projekt.
     *
     * @throws ProjectNotFoundException      wenn es das Projekt nicht gibt (404)
     * @throws EmployeeNotInProjectException wenn der Mitarbeiter nicht im Projekt ist (404)
     */
    @Transactional
    public ProjectResponseDto removeEmployee(Long projectId, Long employeeId) {
        // 1. Gibt es das Projekt?
        ProjectEntity project = repository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        // 2. Die Zuordnung dieses Mitarbeiters im Projekt suchen
        ProjectEmployeeEntity assignment = null;
        for (ProjectEmployeeEntity projectEmployee : project.getEmployees()) {
            if (projectEmployee.getEmployeeId().equals(employeeId)) {
                assignment = projectEmployee;
            }
        }

        // 3. Ist der Mitarbeiter gar nicht im Projekt?
        if (assignment == null) {
            throw new EmployeeNotInProjectException(projectId, employeeId);
        }

        // 4. Entfernen und speichern (die Zeile in project_employee wird gelöscht)
        project.removeEmployee(assignment);
        ProjectEntity saved = repository.save(project);
        return mapper.toDto(saved);
    }
    /**
     * Liefert die Mitarbeiter eines Projekts.
     *
     * @throws ProjectNotFoundException wenn es das Projekt nicht gibt (404)
     */
    @Transactional(readOnly = true)
    public ProjectEmployeesResponseDto getEmployees(Long projectId) {
        ProjectEntity project = repository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        List<ProjectEmployeeDto> employees = project.getEmployees().stream()
                .map(mapper::toDto)
                .toList();
        return new ProjectEmployeesResponseDto(project.getId(), project.getName(), employees);
    }

    /**
     * Liefert alle Projekte eines Mitarbeiters mit seiner Qualifikation im jeweiligen Projekt.
     *
     * @throws EmployeeNotFoundException wenn es den Mitarbeiter im Employee-Service nicht gibt (404)
     */
    @Transactional(readOnly = true)
    public EmployeeProjectsResponseDto getProjectsOfEmployee(Long employeeId) {
        // 1. Gibt es den Mitarbeiter im Employee-Service?
        if (!employeeClient.employeeExists(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }

        // 2. Alle Projekte holen, in denen er eingeplant ist, und je Projekt seine Qualifikation suchen
        List<EmployeeProjectDto> projects = repository.findAllByEmployeeId(employeeId).stream()
                .map(project -> new EmployeeProjectDto(
                        project.getId(),
                        project.getName(),
                        project.getStartDate(),
                        project.getEndDate(),
                        qualificationOf(project, employeeId)))
                .toList();
        return new EmployeeProjectsResponseDto(employeeId, projects);
    }

    /** Sucht die Qualifikation, mit der der Mitarbeiter in diesem Projekt eingesetzt ist. */
    private String qualificationOf(ProjectEntity project, Long employeeId) {
        for (ProjectEmployeeEntity projectEmployee : project.getEmployees()) {
            if (projectEmployee.getEmployeeId().equals(employeeId)) {
                return projectEmployee.getQualification();
            }
        }
        return null;
    }
}
