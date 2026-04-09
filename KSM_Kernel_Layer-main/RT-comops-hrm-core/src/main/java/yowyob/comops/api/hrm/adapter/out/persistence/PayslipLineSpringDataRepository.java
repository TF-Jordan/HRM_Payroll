package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayslipLineSpringDataRepository extends ReactiveCrudRepository<PayslipLineEntity, UUID> {

    Flux<PayslipLineEntity> findAllByTenantIdAndPayrollEntryIdOrderBySortOrderAsc(UUID tenantId, UUID payrollEntryId);

    Mono<Void> deleteAllByPayrollEntryId(UUID payrollEntryId);
}
