package poly.edu.vantix.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final PayrollRepository payrollRepository;
    private final Path contractUploadRoot;

    // Điều 25 BLLĐ 2019 - thử việc tối đa 180 ngày cho quản lý
    private static final int MAX_PROBATION_MONTHS = 6;

    // Điều 20 BLLĐ 2019 - hợp đồng xác định thời hạn tối đa 36 tháng
    private static final int MAX_FIXED_TERM_MONTHS = 36;

    public ContractService(
            ContractRepository contractRepository,
            EmployeeRepository employeeRepository,
            PositionRepository positionRepository,
            PayrollRepository payrollRepository,
            @Value("${app.upload.contract-dir:uploads/contracts}") String contractUploadDir
    ) {
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
        this.payrollRepository = payrollRepository;
        this.contractUploadRoot = Paths.get(contractUploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public List<ContractResponse> search(
            String keyword,
            Long employeeId,
            ContractType contractType,
            ContractStatus status
    ) {
        expireElapsedActiveContracts();
        return contractRepository.search(keyword, employeeId, contractType, status)
                .stream()
                .map(ContractResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PageResponse<ContractResponse> searchPage(
            String keyword,
            Long employeeId,
            ContractType contractType,
            ContractStatus status,
            Pageable pageable
    ) {
        expireElapsedActiveContracts();
        return PageResponse.from(
                contractRepository.search(keyword, employeeId, contractType, status, pageable),
                ContractResponse::fromEntity
        );
    }

    @Transactional
    public ContractResponse getById(Long id) {
        expireElapsedActiveContracts();
        return ContractResponse.fromEntity(findActiveById(id));
    }

    @Transactional
    public List<ContractResponse> getByEmployee(Long employeeId) {
        expireElapsedActiveContracts();
        return contractRepository.findByEmployee(employeeId)
                .stream()
                .map(ContractResponse::fromEntity)
                .toList();
    }

    @Transactional
    public List<ContractResponse> getExpiringContracts(int daysAhead) {
        expireElapsedActiveContracts();
        LocalDate today = LocalDate.now();
        return contractRepository.findExpiringContracts(today, today.plusDays(daysAhead))
                .stream()
                .map(ContractResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ContractResponse create(ContractRequest request) {
        expireElapsedActiveContracts();
        if (contractRepository.existsByContractCodeAndDeletedFalse(request.getContractCode())) {
            throw new BusinessException("contractCode", "Contract code already exists");
        }

        validateBusinessRules(request);

        Contract contract = new Contract();
        mapRequestToEntity(request, contract);
        contract.setStatus(ContractStatus.DRAFT);

        contract = contractRepository.save(contract);
        return ContractResponse.fromEntity(contract);
    }

    @Transactional
    public ContractResponse update(Long id, ContractRequest request) {
        expireElapsedActiveContracts();
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
        expireElapsedActiveContracts();
        Contract contract = findActiveById(id);
        if (contract.getStatus() == ContractStatus.TERMINATED
                || contract.getStatus() == ContractStatus.LIQUIDATED) {
            throw new BusinessException("Cannot activate a terminated/liquidated contract");
        }
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new BusinessException("Only draft contracts can be activated");
        }
        if (!hasSignedFile(contract)) {
            throw new BusinessException("file", "Signed contract file is required before activation");
        }
        if (contract.getEndDate() != null && contract.getEndDate().isBefore(LocalDate.now())) {
            throw new BusinessException("endDate", "Cannot activate a contract that has already expired");
        }

        contract.setStatus(ContractStatus.ACTIVE);
        supersedePreviousActiveContracts(contract.getEmployee().getId(), contract.getId(), contract.getStartDate());

        return ContractResponse.fromEntity(contractRepository.save(contract));
    }

    @Transactional
    public ContractResponse renew(Long id, LocalDate newEndDate) {
        expireElapsedActiveContracts();
        Contract contract = findActiveById(id);

        if (contract.getStatus() != ContractStatus.ACTIVE
                && contract.getStatus() != ContractStatus.EXPIRED) {
            throw new BusinessException("Only active or expired contracts can be renewed");
        }
        if (contract.getEndDate() == null) {
            throw new BusinessException("endDate", "Only contracts with an end date can be renewed");
        }

        LocalDate today = LocalDate.now();
        if (!newEndDate.isAfter(contract.getEndDate())) {
            throw new BusinessException("newEndDate", "New end date must be after current end date");
        }
        if (!newEndDate.isAfter(today)) {
            throw new BusinessException("newEndDate", "New end date must be after today");
        }

        contract.setEndDate(newEndDate);
        contract.setStatus(ContractStatus.DRAFT);
        contract.setTerminatedDate(null);
        contract.setTerminationReason(null);

        return ContractResponse.fromEntity(contractRepository.save(contract));
    }

    @Transactional
    public ContractResponse terminate(Long id, LocalDate terminatedDate, String reason) {
        expireElapsedActiveContracts();
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
    public ContractResponse liquidate(Long id) {
        expireElapsedActiveContracts();
        Contract contract = findActiveById(id);
        if (contract.getStatus() == ContractStatus.LIQUIDATED) {
            return ContractResponse.fromEntity(contract);
        }
        if (contract.getStatus() != ContractStatus.EXPIRED
                && contract.getStatus() != ContractStatus.TERMINATED) {
            throw new BusinessException("Only expired or terminated contracts can be liquidated");
        }

        contract.setStatus(ContractStatus.LIQUIDATED);
        return ContractResponse.fromEntity(contractRepository.save(contract));
    }

    @Transactional
    public ContractResponse uploadSignedFile(Long id, MultipartFile file) {
        expireElapsedActiveContracts();
        Contract contract = findActiveById(id);
        ensureNoPayrollRows(contract);

        if (file == null || file.isEmpty()) {
            throw new BusinessException("file", "Signed contract file is required");
        }

        String contentType = file.getContentType();
        String uploadedFileName = sanitizeFileName(file.getOriginalFilename());
        boolean isPdf = (contentType != null && MediaTypes.APPLICATION_PDF.equalsIgnoreCase(contentType))
                || uploadedFileName.toLowerCase(Locale.ROOT).endsWith(".pdf");
        if (!isPdf) {
            throw new BusinessException("file", "Signed contract file must be a PDF");
        }

        String previousStoredFileName = contract.getAttachmentPath();
        String originalFileName = contractFileName(contract.getContractCode());
        String storedFileName = storeFile(file);
        contract.setAttachmentPath(storedFileName);
        contract.setAttachmentOriginalFileName(originalFileName);
        contract.setAttachmentContentType(contentType == null || contentType.isBlank()
                ? MediaTypes.APPLICATION_PDF
                : contentType);
        contract.setAttachmentFileSize(file.getSize());

        Contract saved = contractRepository.save(contract);
        if (previousStoredFileName != null && !previousStoredFileName.isBlank()) {
            deleteStoredFile(previousStoredFileName);
        }
        return ContractResponse.fromEntity(saved);
    }

    @Transactional
    public ContractFileDownload loadSignedFile(Long id) {
        expireElapsedActiveContracts();
        Contract contract = findActiveById(id);
        if (contract.getAttachmentPath() == null || contract.getAttachmentPath().isBlank()) {
            throw new BusinessException("Signed contract file does not exist");
        }
        return loadFile(
                contract.getAttachmentPath(),
                contract.getAttachmentOriginalFileName(),
                contract.getAttachmentContentType(),
                contract.getAttachmentFileSize()
        );
    }

    @Transactional
    public void deleteSignedFile(Long id) {
        expireElapsedActiveContracts();
        Contract contract = findActiveById(id);
        ensureNoPayrollRows(contract);
        if (contract.getStatus() == ContractStatus.ACTIVE) {
            throw new BusinessException("Cannot delete the signed file of an active contract");
        }

        String storedFileName = contract.getAttachmentPath();
        contract.setAttachmentPath(null);
        contract.setAttachmentOriginalFileName(null);
        contract.setAttachmentContentType(null);
        contract.setAttachmentFileSize(null);
        contractRepository.save(contract);

        if (storedFileName != null && !storedFileName.isBlank()) {
            deleteStoredFile(storedFileName);
        }
    }

    @Transactional
    public void delete(Long id) {
        expireElapsedActiveContracts();
        Contract contract = findActiveById(id);
        ensureNoPayrollRows(contract);
        if (contract.getStatus() != ContractStatus.DRAFT
                && contract.getStatus() != ContractStatus.LIQUIDATED) {
            throw new BusinessException("Only draft or liquidated contracts can be deleted");
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

    @Transactional
    public int expireElapsedActiveContracts() {
        LocalDate today = LocalDate.now();
        List<Contract> contracts = contractRepository.findElapsedActiveContracts(today);
        if (contracts.isEmpty()) {
            return 0;
        }

        contracts.forEach(contract -> contract.setStatus(ContractStatus.EXPIRED));
        contractRepository.saveAll(contracts);
        return contracts.size();
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

    private boolean hasSignedFile(Contract contract) {
        return contract.getAttachmentPath() != null && !contract.getAttachmentPath().isBlank();
    }

    private String storeFile(MultipartFile file) {
        try {
            Files.createDirectories(contractUploadRoot);
            String storedFileName = UUID.randomUUID() + ".pdf";
            Files.copy(file.getInputStream(), resolveStoredPath(storedFileName), StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException ex) {
            throw new BusinessException("file", "Unable to store uploaded file");
        }
    }

    private ContractFileDownload loadFile(String storedFileName, String originalFileName, String contentType, Long fileSize) {
        Path filePath = resolveStoredPath(storedFileName);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("File is not available");
            }
            return new ContractFileDownload(resource, originalFileName, contentType, fileSize);
        } catch (MalformedURLException ex) {
            throw new BusinessException("File is not available");
        }
    }

    private void deleteStoredFile(String storedFileName) {
        try {
            Files.deleteIfExists(resolveStoredPath(storedFileName));
        } catch (IOException ignored) {
        }
    }

    private Path resolveStoredPath(String storedFileName) {
        Path target = contractUploadRoot.resolve(storedFileName).normalize();
        if (!target.startsWith(contractUploadRoot)) {
            throw new BusinessException("Invalid file path");
        }
        return target;
    }

    private String sanitizeFileName(String fileName) {
        String value = fileName == null || fileName.isBlank()
                ? "contract-file.pdf"
                : Paths.get(fileName).getFileName().toString();
        value = value.replaceAll("[^A-Za-z0-9._-]", "_");
        return value.isBlank() ? "contract-file.pdf" : value;
    }

    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index);
    }

    private String contractFileName(String contractCode) {
        String value = contractCode == null || contractCode.isBlank()
                ? "contract"
                : contractCode.trim();
        value = value.replaceAll("[^A-Za-z0-9._-]", "_");
        if (value.isBlank()) {
            value = "contract";
        }
        return value + ".pdf";
    }

    private static final class MediaTypes {
        private static final String APPLICATION_PDF = "application/pdf";

        private MediaTypes() {
        }
    }

    public record ContractFileDownload(
            Resource resource,
            String fileName,
            String contentType,
            Long fileSize
    ) {}
}
