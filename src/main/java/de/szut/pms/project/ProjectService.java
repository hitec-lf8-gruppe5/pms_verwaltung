package de.szut.pms.project;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.szut.pms.customer.CustomerClient;
import de.szut.pms.customer.CustomerNotFoundException;
import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeNotFoundException;

/**
 * Fachlogik für Projekte: Prüfungen gegen andere Services, dann Speichern.
 * Der Controller kümmert sich nur um HTTP, das Repository nur um die Datenbank.
 */
@Service
public class ProjectService {

    private final ProjectRepository repository;
    private final ProjectMapper mapper;
    private final EmployeeClient employeeClient;
    private final CustomerClient customerClient;

    public ProjectService(ProjectRepository repository,
                          ProjectMapper mapper,
                          EmployeeClient employeeClient,
                          CustomerClient customerClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.employeeClient = employeeClient;
        this.customerClient = customerClient;
    }

    /**
     * Legt ein neues Projekt an.
     *
     * @throws EmployeeNotFoundException wenn der verantwortliche Mitarbeiter unbekannt ist
     * @throws CustomerNotFoundException wenn der Kunde unbekannt ist
     */
    @Transactional
    public ProjectResponseDto create(ProjectRequestDto dto) {
        validateReferences(dto);
        ProjectEntity saved = repository.save(mapper.toEntity(dto));
        return mapper.toDto(saved);
    }

    /** Liefert alle Projekte (nach Id sortiert) mit allen Daten inkl. Mitarbeiter. Keine Projekte → leere Liste. */
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> findAll() {
        return repository.findAll(Sort.by("id")).stream()
                .map(mapper::toDto)
                .toList();
    }

    /**
     * Liefert ein bestimmtes Projekt.
     *
     * @throws ProjectNotFoundException wenn es kein Projekt mit dieser Id gibt
     */
    @Transactional(readOnly = true)
    public ProjectResponseDto findById(Long id) {
        ProjectEntity entity = repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        return mapper.toDto(entity);
    }

    /**
     * Überschreibt alle Daten eines bestehenden Projekts (PUT). Die Id und die
     * zugeordneten Mitarbeiter bleiben erhalten.
     *
     * @throws ProjectNotFoundException  wenn es kein Projekt mit dieser Id gibt
     * @throws EmployeeNotFoundException wenn der verantwortliche Mitarbeiter unbekannt ist
     * @throws CustomerNotFoundException wenn der Kunde unbekannt ist
     */
    @Transactional
    public ProjectResponseDto update(Long id, ProjectRequestDto dto) {
        ProjectEntity entity = repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        validateReferences(dto);
        mapper.updateEntity(entity, dto);
        ProjectEntity saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    /**
     * Prüft die Ids, die auf andere Services verweisen. Wird auch beim
     * Ändern eines Projekts (PUT) wiederverwendet.
     */
    private void validateReferences(ProjectRequestDto dto) {
        if (!employeeClient.employeeExists(dto.responsibleEmployeeId())) {
            throw new EmployeeNotFoundException(dto.responsibleEmployeeId());
        }
        if (!customerClient.existsById(dto.customerId())) {
            throw new CustomerNotFoundException(dto.customerId());
        }
    }

    /**
     * Löscht ein Projekt inklusive seiner Mitarbeiterzuordnungen (Cascade).
     *
     * @throws ProjectNotFoundException wenn es kein Projekt mit dieser Id gibt
     */
    @Transactional
    public void delete(Long id) {
        ProjectEntity entity = repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        repository.delete(entity);
    }
}
