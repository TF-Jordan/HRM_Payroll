-- =============================================================================
-- V34: HRM Core — Human Resource Management & Payroll
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS hrm;

-- -----------------------------------------------------------------------------
-- 1. hrm_employee
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.employee (
    id                  uuid        PRIMARY KEY,
    tenant_id           uuid        NOT NULL,
    created_at          timestamptz NOT NULL,
    updated_at          timestamptz NOT NULL,
    organization_id     uuid        NOT NULL,
    agency_id           uuid,
    actor_id            uuid        NOT NULL,
    registration_number text        NOT NULL,
    first_name          text        NOT NULL,
    last_name           text        NOT NULL,
    email               text,
    phone_number        text,
    gender              text,
    birth_date          date,
    hire_date           date        NOT NULL,
    termination_date    date,
    department          text,
    job_title           text,
    status              text        NOT NULL DEFAULT 'ACTIVE',
    cnps_number         text,
    UNIQUE (tenant_id, organization_id, registration_number)
);

CREATE INDEX IF NOT EXISTS idx_hrm_employee_org
    ON hrm.employee (tenant_id, organization_id);

CREATE INDEX IF NOT EXISTS idx_hrm_employee_agency
    ON hrm.employee (tenant_id, organization_id, agency_id);

CREATE INDEX IF NOT EXISTS idx_hrm_employee_actor
    ON hrm.employee (tenant_id, actor_id);

-- -----------------------------------------------------------------------------
-- 2. hrm_contract
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.contract (
    id                  uuid           PRIMARY KEY,
    tenant_id           uuid           NOT NULL,
    created_at          timestamptz    NOT NULL,
    updated_at          timestamptz    NOT NULL,
    organization_id     uuid           NOT NULL,
    employee_id         uuid           NOT NULL REFERENCES hrm.employee(id),
    contract_type       text           NOT NULL,
    start_date          date           NOT NULL,
    end_date            date,
    base_salary         numeric(19,2)  NOT NULL,
    currency            text           NOT NULL DEFAULT 'XAF',
    status              text           NOT NULL DEFAULT 'DRAFT',
    UNIQUE (tenant_id, organization_id, employee_id, start_date)
);

CREATE INDEX IF NOT EXISTS idx_hrm_contract_employee
    ON hrm.contract (tenant_id, employee_id);

-- -----------------------------------------------------------------------------
-- 3. hrm_dependent
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.dependent (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    organization_id uuid        NOT NULL,
    employee_id     uuid        NOT NULL REFERENCES hrm.employee(id),
    first_name      text        NOT NULL,
    last_name       text        NOT NULL,
    relationship    text        NOT NULL,
    birth_date      date,
    gender          text
);

CREATE INDEX IF NOT EXISTS idx_hrm_dependent_employee
    ON hrm.dependent (tenant_id, employee_id);

-- -----------------------------------------------------------------------------
-- 4. hrm_leave_balance
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.leave_balance (
    id              uuid           PRIMARY KEY,
    tenant_id       uuid           NOT NULL,
    created_at      timestamptz    NOT NULL,
    updated_at      timestamptz    NOT NULL,
    organization_id uuid           NOT NULL,
    employee_id     uuid           NOT NULL REFERENCES hrm.employee(id),
    leave_type      text           NOT NULL,
    year            int            NOT NULL,
    accrued         numeric(10,2)  NOT NULL DEFAULT 0,
    taken           numeric(10,2)  NOT NULL DEFAULT 0,
    adjustment      numeric(10,2)  NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, organization_id, employee_id, leave_type, year)
);

CREATE INDEX IF NOT EXISTS idx_hrm_leave_balance_employee
    ON hrm.leave_balance (tenant_id, employee_id, year);

-- -----------------------------------------------------------------------------
-- 5. hrm_leave_request
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.leave_request (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    organization_id uuid        NOT NULL,
    employee_id     uuid        NOT NULL REFERENCES hrm.employee(id),
    leave_type      text        NOT NULL,
    start_date      date        NOT NULL,
    end_date        date        NOT NULL,
    days_requested  numeric(10,2) NOT NULL,
    reason          text,
    status          text        NOT NULL DEFAULT 'PENDING',
    approved_by     uuid,
    approved_at     timestamptz,
    rejection_reason text
);

CREATE INDEX IF NOT EXISTS idx_hrm_leave_request_employee
    ON hrm.leave_request (tenant_id, employee_id);

CREATE INDEX IF NOT EXISTS idx_hrm_leave_request_status
    ON hrm.leave_request (tenant_id, organization_id, status);

-- -----------------------------------------------------------------------------
-- 6. hrm_loan_advance
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.loan_advance (
    id                   uuid           PRIMARY KEY,
    tenant_id            uuid           NOT NULL,
    created_at           timestamptz    NOT NULL,
    updated_at           timestamptz    NOT NULL,
    organization_id      uuid           NOT NULL,
    employee_id          uuid           NOT NULL REFERENCES hrm.employee(id),
    loan_type            text           NOT NULL,
    amount               numeric(19,2)  NOT NULL,
    currency             text           NOT NULL DEFAULT 'XAF',
    monthly_deduction    numeric(19,2)  NOT NULL,
    total_repaid         numeric(19,2)  NOT NULL DEFAULT 0,
    remaining_balance    numeric(19,2)  NOT NULL,
    installments_count   int            NOT NULL,
    installments_paid    int            NOT NULL DEFAULT 0,
    status               text           NOT NULL DEFAULT 'PENDING',
    approved_by          uuid,
    approved_at          timestamptz
);

CREATE INDEX IF NOT EXISTS idx_hrm_loan_employee
    ON hrm.loan_advance (tenant_id, employee_id);

CREATE INDEX IF NOT EXISTS idx_hrm_loan_status
    ON hrm.loan_advance (tenant_id, organization_id, status);

-- -----------------------------------------------------------------------------
-- 7. hrm_payroll_run
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.payroll_run (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    organization_id uuid        NOT NULL,
    agency_id       uuid,
    period          text        NOT NULL,
    status          text        NOT NULL DEFAULT 'DRAFT',
    calculated_at   timestamptz,
    validated_by    uuid,
    validated_at    timestamptz,
    paid_at         timestamptz,
    total_gross     numeric(19,2) NOT NULL DEFAULT 0,
    total_net       numeric(19,2) NOT NULL DEFAULT 0,
    total_employer_charges numeric(19,2) NOT NULL DEFAULT 0,
    currency        text        NOT NULL DEFAULT 'XAF',
    employee_count  int         NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, organization_id, period)
);

CREATE INDEX IF NOT EXISTS idx_hrm_payroll_run_org
    ON hrm.payroll_run (tenant_id, organization_id);

-- -----------------------------------------------------------------------------
-- 8. hrm_payroll_entry
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.payroll_entry (
    id              uuid           PRIMARY KEY,
    tenant_id       uuid           NOT NULL,
    created_at      timestamptz    NOT NULL,
    updated_at      timestamptz    NOT NULL,
    payroll_run_id  uuid           NOT NULL REFERENCES hrm.payroll_run(id),
    employee_id     uuid           NOT NULL REFERENCES hrm.employee(id),
    contract_id     uuid           NOT NULL REFERENCES hrm.contract(id),
    base_salary     numeric(19,2)  NOT NULL,
    gross_salary    numeric(19,2)  NOT NULL DEFAULT 0,
    net_salary      numeric(19,2)  NOT NULL DEFAULT 0,
    total_deductions numeric(19,2) NOT NULL DEFAULT 0,
    total_employer_charges numeric(19,2) NOT NULL DEFAULT 0,
    currency        text           NOT NULL DEFAULT 'XAF',
    UNIQUE (tenant_id, payroll_run_id, employee_id)
);

CREATE INDEX IF NOT EXISTS idx_hrm_payroll_entry_run
    ON hrm.payroll_entry (tenant_id, payroll_run_id);

CREATE INDEX IF NOT EXISTS idx_hrm_payroll_entry_employee
    ON hrm.payroll_entry (tenant_id, employee_id);

-- -----------------------------------------------------------------------------
-- 9. hrm_payslip_line
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.payslip_line (
    id                uuid           PRIMARY KEY,
    tenant_id         uuid           NOT NULL,
    created_at        timestamptz    NOT NULL,
    updated_at        timestamptz    NOT NULL,
    payroll_entry_id  uuid           NOT NULL REFERENCES hrm.payroll_entry(id),
    code              text           NOT NULL,
    label             text           NOT NULL,
    line_type         text           NOT NULL,
    base              numeric(19,2),
    rate              numeric(10,4),
    employee_amount   numeric(19,2)  NOT NULL DEFAULT 0,
    employer_amount   numeric(19,2)  NOT NULL DEFAULT 0,
    sort_order        int            NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_hrm_payslip_line_entry
    ON hrm.payslip_line (tenant_id, payroll_entry_id);

-- -----------------------------------------------------------------------------
-- 10. hrm_training
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.training (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    organization_id uuid        NOT NULL,
    title           text        NOT NULL,
    description     text,
    start_date      date,
    end_date        date,
    max_participants int,
    status          text        NOT NULL DEFAULT 'PLANNED'
);

CREATE INDEX IF NOT EXISTS idx_hrm_training_org
    ON hrm.training (tenant_id, organization_id);

-- -----------------------------------------------------------------------------
-- 11. hrm_training_enrollment
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.training_enrollment (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    training_id     uuid        NOT NULL REFERENCES hrm.training(id),
    employee_id     uuid        NOT NULL REFERENCES hrm.employee(id),
    enrollment_status text      NOT NULL DEFAULT 'ENROLLED',
    completed_at    timestamptz,
    score           numeric(5,2),
    UNIQUE (tenant_id, training_id, employee_id)
);

CREATE INDEX IF NOT EXISTS idx_hrm_enrollment_training
    ON hrm.training_enrollment (tenant_id, training_id);

CREATE INDEX IF NOT EXISTS idx_hrm_enrollment_employee
    ON hrm.training_enrollment (tenant_id, employee_id);

-- -----------------------------------------------------------------------------
-- 12. hrm_performance_review
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.performance_review (
    id              uuid        PRIMARY KEY,
    tenant_id       uuid        NOT NULL,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    organization_id uuid        NOT NULL,
    employee_id     uuid        NOT NULL REFERENCES hrm.employee(id),
    reviewer_id     uuid        NOT NULL,
    review_period   text        NOT NULL,
    overall_rating  numeric(3,1),
    comments        text,
    status          text        NOT NULL DEFAULT 'DRAFT'
);

CREATE INDEX IF NOT EXISTS idx_hrm_review_employee
    ON hrm.performance_review (tenant_id, employee_id);

CREATE INDEX IF NOT EXISTS idx_hrm_review_org
    ON hrm.performance_review (tenant_id, organization_id);

-- -----------------------------------------------------------------------------
-- 13. hrm_review_objective
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hrm.review_objective (
    id              uuid           PRIMARY KEY,
    tenant_id       uuid           NOT NULL,
    created_at      timestamptz    NOT NULL,
    updated_at      timestamptz    NOT NULL,
    review_id       uuid           NOT NULL REFERENCES hrm.performance_review(id),
    title           text           NOT NULL,
    description     text,
    weight          numeric(5,2)   NOT NULL DEFAULT 1,
    rating          numeric(3,1),
    comments        text
);

CREATE INDEX IF NOT EXISTS idx_hrm_objective_review
    ON hrm.review_objective (tenant_id, review_id);
