package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.domain.model.Employee;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class EmployeeR2dbcRepositoryAdapter implements EmployeeRepository {

    private final EmployeeSpringDataRepository repository;

    public EmployeeR2dbcRepositoryAdapter(EmployeeSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Employee> findById(UUID employeeId) {
        return repository.findById(employeeId).map(this::toDomain);
    }

    @Override
    public Flux<Employee> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Flux<Employee> findByOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByRegistrationNumber(UUID tenantId, UUID organizationId, String registrationNumber) {
        return repository.existsByTenantIdAndOrganizationIdAndRegistrationNumber(tenantId, organizationId,
                registrationNumber);
    }

    @Override
    public Mono<Boolean> existsByRegistrationNumberExcludingId(UUID tenantId, UUID organizationId,
            String registrationNumber, UUID employeeId) {
        return repository.existsByTenantIdAndOrganizationIdAndRegistrationNumberAndIdNot(tenantId, organizationId,
                registrationNumber, employeeId);
    }

    @Override
    public Mono<Employee> save(Employee employee) {
        EmployeeEntity entity = new EmployeeEntity(employee.id(), employee.tenantId(), employee.createdAt(),
                employee.updatedAt(), employee.organizationId(), employee.agencyId(), employee.actorId(),
                employee.registrationNumber(), employee.firstName(), employee.lastName(), employee.email(),
                employee.phoneNumber(), employee.gender(), employee.birthDate(), employee.hireDate(),
                employee.terminationDate(), employee.department(), employee.jobTitle(), employee.status(),
                employee.cnpsNumber());
        return repository.save(entity).map(this::toDomain);
    }

    private Employee toDomain(EmployeeEntity entity) {
        return Employee.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.actorId(), entity.registrationNumber(),
                entity.firstName(), entity.lastName(), entity.email(), entity.phoneNumber(), entity.gender(),
                entity.birthDate(), entity.hireDate(), entity.terminationDate(), entity.department(),
                entity.jobTitle(), entity.status(), entity.cnpsNumber());
    }
}
