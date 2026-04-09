package yowyob.comops.api.hrm.config;

import yowyob.comops.api.hrm.application.service.LeaveAccrualService;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Monthly scheduled job for automatic leave accrual.
 * Runs on the 1st of every month at 02:00 AM by default.
 * Accrues annual leave for all active employees per Cameroon labor code.
 *
 * Enabled via property: {@code hrm.leave-accrual.enabled=true}
 */
@Component
@ConditionalOnProperty(name = "hrm.leave-accrual.enabled", havingValue = "true")
public class LeaveAccrualScheduler {

    private static final Logger log = LoggerFactory.getLogger(LeaveAccrualScheduler.class);

    private final LeaveAccrualService leaveAccrualService;
    private final OrganizationRepository organizationRepository;
    private final UUID systemTenantId;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public LeaveAccrualScheduler(LeaveAccrualService leaveAccrualService,
                                 OrganizationRepository organizationRepository,
                                 @Value("${hrm.leave-accrual.tenant-id:}") String tenantId) {
        this.leaveAccrualService = leaveAccrualService;
        this.organizationRepository = organizationRepository;
        this.systemTenantId = tenantId.isBlank() ? null : UUID.fromString(tenantId);
    }

    @Scheduled(cron = "${hrm.leave-accrual.cron:0 0 2 1 * *}")
    public void accrueMonthlyLeave() {
        if (systemTenantId == null) {
            log.warn("hrm.leave-accrual.tenant-id not configured, skipping leave accrual");
            return;
        }
        if (!running.compareAndSet(false, true)) {
            log.warn("Leave accrual already running, skipping");
            return;
        }
        log.info("Starting monthly leave accrual for tenant {}", systemTenantId);
        organizationRepository.findByTenantId(systemTenantId)
                .flatMap(org -> leaveAccrualService.accrueMonthlyLeave(
                        systemTenantId, org.id()))
                .doOnComplete(() -> log.info("Monthly leave accrual completed"))
                .doOnError(error -> log.error("Leave accrual failed", error))
                .doFinally(signal -> running.set(false))
                .subscribe();
    }
}
