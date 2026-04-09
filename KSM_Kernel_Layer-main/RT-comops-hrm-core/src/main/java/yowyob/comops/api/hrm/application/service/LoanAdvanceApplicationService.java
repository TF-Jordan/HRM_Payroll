package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.in.ApproveLoanAdvanceUseCase;
import yowyob.comops.api.hrm.application.port.in.CreateLoanAdvanceCommand;
import yowyob.comops.api.hrm.application.port.in.CreateLoanAdvanceUseCase;
import yowyob.comops.api.hrm.application.port.in.GetLoanAdvanceUseCase;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LoanAdvanceRepository;
import yowyob.comops.api.hrm.domain.exception.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.exception.LoanAdvanceNotFoundException;
import yowyob.comops.api.hrm.domain.model.LoanAdvance;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LoanAdvanceApplicationService implements CreateLoanAdvanceUseCase, ApproveLoanAdvanceUseCase,
        GetLoanAdvanceUseCase {

    private final LoanAdvanceRepository loanAdvanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public LoanAdvanceApplicationService(LoanAdvanceRepository loanAdvanceRepository,
                                         EmployeeRepository employeeRepository,
                                         ReactiveTransactionalExecutor transactionalExecutor) {
        this.loanAdvanceRepository = loanAdvanceRepository;
        this.employeeRepository = employeeRepository;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<LoanAdvance> createLoanAdvance(CreateLoanAdvanceCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(
                employeeRepository.findById(command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .flatMap(employee -> {
                            LoanAdvance loan = LoanAdvance.create(command.tenantId(), command.organizationId(),
                                    command.employeeId(), command.loanType(), command.amount(),
                                    command.currency(), command.monthlyDeduction(), command.installmentsCount());
                            return loanAdvanceRepository.save(loan);
                        }));
    }

    @Override
    public Mono<LoanAdvance> approveLoanAdvance(UUID loanId, UUID approvedBy) {
        return transactionalExecutor.transactional(
                loanAdvanceRepository.findById(loanId)
                        .switchIfEmpty(Mono.error(new LoanAdvanceNotFoundException(loanId)))
                        .map(loan -> loan.approve(approvedBy))
                        .flatMap(loanAdvanceRepository::save));
    }

    @Override
    public Mono<LoanAdvance> getLoanAdvance(UUID loanId) {
        return loanAdvanceRepository.findById(loanId)
                .switchIfEmpty(Mono.error(new LoanAdvanceNotFoundException(loanId)));
    }

    @Override
    public Flux<LoanAdvance> getLoansByEmployee(UUID tenantId, UUID employeeId) {
        return loanAdvanceRepository.findByEmployeeId(tenantId, employeeId);
    }
}
