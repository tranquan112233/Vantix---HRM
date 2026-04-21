package poly.edu.vantix.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.ContractRequest;
import poly.edu.vantix.dto.response.ContractResponse;
import poly.edu.vantix.dto.response.PageResponse;
import poly.edu.vantix.entity.Contract;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.Position;
import poly.edu.vantix.entity.enums.ContractStatus;
import poly.edu.vantix.entity.enums.ContractType;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.ContractRepository;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.PayrollRepository;
import poly.edu.vantix.repository.PositionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final PayrollRepository payrollRepository;

    // Điều 25 BLLĐ 2019 - thử việc tối đa 180 ngày cho quản lý
    private static final int MAX_PROBATION_MONTHS = 6;

    // Điều 20 BLLĐ 2019 - hợp đồng xác định thời hạn tối đa 36 tháng
    private static final int MAX_FIXED_TERM_MONTHS = 36;

    public ContractService(
            ContractRepository contractRepository,
            EmployeeRepository employeeRepository,
            PositionRepository positionRepository,
            PayrollRepository payrollRepository
    ) {
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
        this.payrollRepository = payrollRepository;
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> search(
            String keyword,
            Long employeeId,
            ContractType contractType,
            ContractStatus status
    ) {
        return contractRepository.search(keyword, employeeId, contractType, status)
                .stream()
                .map(ContractResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ContractResponse> searchPage(
            String keyword,
            Long employeeId,
            ContractType contractType,
            ContractStatus status,
            Pageable pageable
    ) {
        return PageResponse.from(
                contractRepository.search(keyword, employeeId, contractType, status, pageable),
                ContractResponse::fromEntity
        );
    }

    @Transactional(readOnly = true)
    public ContractResponse getById(Long id) {
        return ContractResponse.fromEntity(findActiveById(id));
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getByEmployee(Long employeeId) {
        return contractRepository.findByEmployee(employeeId)
                .stream()
                .map(ContractResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getExpiringContracts(int daysAhead) {
        LocalDate today = LocalDate.now();
        return contractRepository.findExpiringContracts(today, today.plusDays(daysAhead))
                .stream()
                .map(ContractResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ContractResponse create(ContractRequest request) {
        if (contractRepository.existsByContractCodeAndDeletedFalse(request.getContractCode())) {
            throw new BusinessException("contractCode", "Contract code already exists");
        }

        validateBusinessRules(request);

        Contract contract = new Contract();
        mapRequestToEntity(request, contract);

        if (request.getStatus() == null) {
            contract.setStatus(ContractStatus.DRAFT);
        }

        // Nếu trạng thái ACTIVE thì các HĐ ACTIVE khác của nhân viên này phải chuyển EXPIRED
        if (contract.getStatus() == ContractStatus.ACTIVE) {
            supersedePreviousActiveContracts(contract.getEmployee().getId(), null, contract.getStartDate());
        }

        contract = contractRepository.save(contract);
        return ContractResponse.fromEntity(contract);
    }

    @Transactional
    public ContractResponse update(Long id, ContractRequest request) {
        Contract contract = findActiveById(id);
        ensureNoPayrollRows(contract);

        contractRepository.findByContractCode(request.getContractCode())
                .filter(c -> !c.getId().equals(id) && !c.getDeleted())
                .ifPresent(c -> {
                    throw new BusinessException("contractCode", "Contract code already exists");
                });

        validateBusinessRules(request);
        mapRequestToEntity(request, contract);

        if (contract.getStatus() == ContractStatus.ACTIVE) {
            supersedePreviousActiveContracts(contract.getEmployee().getId(), contract.getId(), contract.getStartDate());
        }

        contract = contractRepository.save(contract);
        return ContractResponse.fromEntity(contract);
    }

    @Transactional
    public ContractResponse activate(Long id) {
        Contract contract = findActiveById(id);
        if (contract.getStatus() == ContractStatus.TERMINATED
                || contract.getStatus() == ContractStatus.LIQUIDATED) {
            throw new BusinessException("Cannot activate a terminated/liquidated contract");
        }

        contract.setStatus(ContractStatus.ACTIVE);
        supersedePreviousActiveContracts(contract.getEmployee().getId(), contract.getId(), contract.getStartDate());

        return ContractResponse.fromEntity(contractRepository.save(contract));
    }

    @Transactional
    public ContractResponse terminate(Long id, LocalDate terminatedDate, String reason) {
        Contract contract = findActiveById(id);
        if (contract.getStatus() == ContractStatus.TERMINATED
                || contract.getStatus() == ContractStatus.LIQUIDATED) {
            throw new BusinessException("Contract is already terminated");
        }

        LocalDate effective = terminatedDate == null ? LocalDate.now() : terminatedDate;
        if (effective.isBefore(contract.getStartDate())) {
            throw new BusinessException("terminatedDate", "Termination date must be after start date");
        }

        boolean wasActive = contract.getStatus() == ContractStatus.ACTIVE;
        contract.setStatus(ContractStatus.TERMINATED);
        contract.setTerminatedDate(effective);
        contract.setTerminationReason(reason);

        Contract saved = contractRepository.save(contract);
        if (wasActive && !effective.isAfter(LocalDate.now())) {
            Employee employee = saved.getEmployee();
            employee.setStatus(EmploymentStatus.TERMINATED);
            employee.setTerminationDate(effective);
            employeeRepository.save(employee);
        }

        return ContractResponse.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        Contract contract = findActiveById(id);
        ensureNoPayrollRows(contract);
        if (contract.getStatus() == ContractStatus.ACTIVE) {
            throw new BusinessException("Cannot delete an active contract. Terminate it first.");
        }
        contract.setDeleted(true);
        contract.setDeletedAt(LocalDateTime.now());
        contractRepository.save(contract);
    }

    // ==================== helpers ====================

    private Contract findActiveById(Long id) {
        return contractRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("Contract not found with id: " + id));
    }

    private void ensureNoPayrollRows(Contract contract) {
        if (payrollRepository.countByContractIdAndDeletedFalse(contract.getId()) > 0) {
            throw new BusinessException("Contract already has payroll data and cannot be changed directly");
        }
    }

    private void mapRequestToEntity(ContractRequest request, Contract contract) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException("employeeId", "Employee does not exist"));
        if (Boolean.TRUE.equals(employee.getDeleted())) {
            throw new BusinessException("employeeId", "Employee has been deleted");
        }

        contract.setContractCode(request.getContractCode());
        contract.setEmployee(employee);

        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new BusinessException("positionId", "Position does not exist"));
            contract.setPosition(position);
        } else {
            contract.setPosition(employee.getPosition());
        }

        contract.setContractType(request.getContractType());
        if (request.getStatus() != null) {
            contract.setStatus(request.getStatus());
        }

        contract.setSignedDate(request.getSignedDate());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setProbationMonths(request.getProbationMonths());

        contract.setBaseSalary(nz(request.getBaseSalary()));
        contract.setInsuranceSalary(
                request.getInsuranceSalary() == null ? nz(request.getBaseSalary()) : request.getInsuranceSalary()
        );
        contract.setResponsibilityAllowance(nz(request.getResponsibilityAllowance()));
        contract.setMealAllowance(nz(request.getMealAllowance()));
        contract.setTransportAllowance(nz(request.getTransportAllowance()));
        contract.setPhoneAllowance(nz(request.getPhoneAllowance()));
        contract.setOtherAllowance(nz(request.getOtherAllowance()));

        if (request.getStandardWorkDays() != null) {
            contract.setStandardWorkDays(request.getStandardWorkDays());
        }
        if (request.getHoursPerDay() != null) {
            contract.setHoursPerDay(request.getHoursPerDay());
        }
        contract.setNoticePeriodDays(request.getNoticePeriodDays());
        contract.setTerminatedDate(request.getTerminatedDate());
        contract.setTerminationReason(request.getTerminationReason());
        contract.setAttachmentPath(request.getAttachmentPath());
        contract.setNote(request.getNote());
    }

    private void validateBusinessRules(ContractRequest request) {
        // Ngày kết thúc phải sau ngày bắt đầu
        if (request.getEndDate() != null && !request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException("endDate", "End date must be after start date");
        }

        // Hợp đồng không xác định thời hạn không được có ngày kết thúc
        if (request.getContractType() == ContractType.INDEFINITE && request.getEndDate() != null) {
            throw new BusinessException("endDate", "Indefinite contract must not have an end date");
        }

        // Hợp đồng xác định thời hạn bắt buộc có ngày kết thúc, tối đa 36 tháng (Điều 20 BLLĐ 2019)
        if (request.getContractType() == ContractType.FIXED_TERM) {
            if (request.getEndDate() == null) {
                throw new BusinessException("endDate", "Fixed-term contract requires an end date");
            }
            long months = ChronoUnit.MONTHS.between(request.getStartDate(), request.getEndDate());
            if (months > MAX_FIXED_TERM_MONTHS) {
                throw new BusinessException("endDate",
                        "Fixed-term contract duration must not exceed " + MAX_FIXED_TERM_MONTHS + " months");
            }
        }

        // Thử việc - tối đa 180 ngày theo Điều 25
        if (request.getContractType() == ContractType.PROBATION) {
            if (request.getEndDate() == null) {
                throw new BusinessException("endDate", "Probation contract requires an end date");
            }
            long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
            if (days > 180) {
                throw new BusinessException("endDate", "Probation period must not exceed 180 days");
            }
        }

        // Thời gian thử việc chung - tối đa 6 tháng
        if (request.getProbationMonths() != null && request.getProbationMonths() > MAX_PROBATION_MONTHS) {
            throw new BusinessException("probationMonths",
                    "Probation months must not exceed " + MAX_PROBATION_MONTHS);
        }

        // Lương cơ bản phải >= lương tối thiểu vùng (4.96M theo Nghị định 74/2024 - vùng I)
        // Ở đây chỉ kiểm tra > 0, để sau admin có thể cấu hình mức tối thiểu
        if (request.getBaseSalary() == null || request.getBaseSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("baseSalary", "Base salary must be greater than 0");
        }
    }

    private void supersedePreviousActiveContracts(Long employeeId, Long currentContractId, LocalDate newStartDate) {
        List<Contract> existing = contractRepository.findActiveContractsByEmployee(employeeId);
        for (Contract c : existing) {
            if (currentContractId != null && c.getId().equals(currentContractId)) {
                continue;
            }
            c.setStatus(ContractStatus.EXPIRED);
            if (c.getEndDate() == null || c.getEndDate().isAfter(newStartDate)) {
                c.setEndDate(newStartDate.minusDays(1));
            }
            contractRepository.save(c);
        }
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
