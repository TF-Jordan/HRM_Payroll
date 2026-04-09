package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PayrollEntrySpringDataRepository extends ReactiveCrudRepository<PayrollEntryEntity, UUID> {

    Flux<PayrollEntryEntity> findAllByTenantIdAndPayrollRunId(UUID tenantId, UUID payrollRunId);
}
