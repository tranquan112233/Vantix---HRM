package poly.edu.vantix.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix.dto.response.EmployeeDocumentResponse;
import poly.edu.vantix.dto.request.EmployeeRequest;
import poly.edu.vantix.dto.response.EmployeeResponse;
import poly.edu.vantix.dto.response.PageResponse;
import poly.edu.vantix.entity.Department;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.EmployeeDocument;
import poly.edu.vantix.entity.Position;
import poly.edu.vantix.entity.Role;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.entity.enums.Gender;
import poly.edu.vantix.entity.enums.UserStatus;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.repository.DepartmentRepository;
import poly.edu.vantix.repository.EmployeeDocumentRepository;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.PositionRepository;
import poly.edu.vantix.repository.RoleRepository;
import poly.edu.vantix.repository.UserRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final PasswordEncoder passwordEncoder;
    private final Path employeeUploadRoot;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            PositionRepository positionRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmployeeDocumentRepository employeeDocumentRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.upload.employee-dir:uploads/employees}") String employeeUploadDir
    ) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeDocumentRepository = employeeDocumentRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeUploadRoot = Paths.get(employeeUploadDir).toAbsolutePath().normalize();
    }

    private static final String DEFAULT_ACCOUNT_PASSWORD = "User@123";
    private static final String DEFAULT_ACCOUNT_ROLE = "Employee";
    private static final String DEFAULT_ACCOUNT_EMAIL_DOMAIN = "vantix.local";

    // Danh sách nhân viên với tìm kiếm
    @Transactional(readOnly = true)
    public List<EmployeeResponse> search(
            String keyword,
            Long departmentId,
            Gender gender,
            EmploymentStatus status
    ) {
        return employeeRepository.search(keyword, departmentId, gender, status)
                .stream()
                .map(EmployeeResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchPage(
            String keyword,
            Long departmentId,
            Gender gender,
            EmploymentStatus status,
            Pageable pageable
    ) {
        return PageResponse.from(
                employeeRepository.search(keyword, departmentId, gender, status, pageable),
                EmployeeResponse::fromEntity
        );
    }

    // Lấy chi tiết nhân viên theo id
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        Employee employee = findActiveById(id);
        return EmployeeResponse.fromEntity(employee);
    }

    // Tạo mới nhân viên
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        // Kiểm tra mã nhân viên trùng
        if (employeeRepository.existsByEmployeeCodeAndDeletedFalse(request.getEmployeeCode())) {
            throw new BusinessException("employeeCode", "Employee code already exists");
        }

        // Kiểm tra CCCD trùng
        if (request.getCitizenId() != null && !request.getCitizenId().isBlank()
                && employeeRepository.existsByCitizenIdAndDeletedFalse(request.getCitizenId())) {
            throw new BusinessException("citizenId", "Citizen ID already exists");
        }

        Employee employee = new Employee();
        mapRequestToEntity(request, employee, true);

        if (request.getStatus() == null) {
            employee.setStatus(EmploymentStatus.ACTIVE);
        }

        employee = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(employee);
    }

    // Cập nhật nhân viên
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findActiveById(id);

        // Kiểm tra mã nhân viên trùng (trừ chính nó)
        employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .filter(e -> !e.getId().equals(id) && !e.getDeleted())
                .ifPresent(e -> {
                    throw new BusinessException("employeeCode", "Employee code already exists");
                });

        mapRequestToEntity(request, employee, false);
        employee = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(employee);
    }

    // Xóa mềm nhân viên
    @Transactional
    public void delete(Long id) {
        Employee employee = findActiveById(id);
        if (employee.getPhotoFileName() != null) {
            deleteStoredFile(employee.getPhotoFileName());
        }
        employee.getDocuments().stream()
                .filter(document -> !Boolean.TRUE.equals(document.getDeleted()))
                .forEach(document -> {
                    document.setDeleted(true);
                    document.setDeletedAt(LocalDateTime.now());
                    deleteStoredFile(document.getStoredFileName());
                });
        employee.setDeleted(true);
        employee.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    @Transactional
    public EmployeeResponse uploadPhoto(Long employeeId, MultipartFile file) {
        Employee employee = findActiveById(employeeId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("file", "Photo is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException("file", "Photo must be an image");
        }

        String previousFileName = employee.getPhotoFileName();
        StoredFile storedFile = storeFile(file);
        employee.setPhotoFileName(storedFile.storedFileName());
        employee.setPhotoOriginalFileName(storedFile.originalFileName());
        employee.setPhotoContentType(file.getContentType());
        employee.setPhotoFileSize(file.getSize());
        Employee saved = employeeRepository.save(employee);
        if (previousFileName != null) {
            deleteStoredFile(previousFileName);
        }
        return EmployeeResponse.fromEntity(saved);
    }

    @Transactional
    public void deletePhoto(Long employeeId) {
        Employee employee = findActiveById(employeeId);
        String storedFileName = employee.getPhotoFileName();
        employee.setPhotoFileName(null);
        employee.setPhotoOriginalFileName(null);
        employee.setPhotoContentType(null);
        employee.setPhotoFileSize(null);
        employeeRepository.save(employee);
        if (storedFileName != null) {
            deleteStoredFile(storedFileName);
        }
    }

    @Transactional(readOnly = true)
    public EmployeeFileDownload loadPhoto(Long employeeId) {
        Employee employee = findActiveById(employeeId);
        if (employee.getPhotoFileName() == null) {
            throw new BusinessException("Photo does not exist");
        }
        return loadFile(
                employee.getPhotoFileName(),
                employee.getPhotoOriginalFileName(),
                employee.getPhotoContentType(),
                employee.getPhotoFileSize()
        );
    }

    @Transactional
    public List<EmployeeDocumentResponse> addDocuments(Long employeeId, MultipartFile[] files) {
        Employee employee = findActiveById(employeeId);
        if (files == null || files.length == 0) {
            throw new BusinessException("files", "At least one file is required");
        }

        List<MultipartFile> validFiles = Arrays.stream(files)
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        if (validFiles.isEmpty()) {
            throw new BusinessException("files", "At least one file is required");
        }

        return validFiles.stream()
                .map(file -> storeDocument(employee, file))
                .map(EmployeeDocumentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteDocument(Long employeeId, Long documentId) {
        EmployeeDocument document = employeeDocumentRepository.findActiveByIdAndEmployeeId(documentId, employeeId)
                .orElseThrow(() -> new BusinessException("Document does not exist"));
        document.setDeleted(true);
        document.setDeletedAt(LocalDateTime.now());
        employeeDocumentRepository.save(document);
        deleteStoredFile(document.getStoredFileName());
    }

    @Transactional(readOnly = true)
    public EmployeeFileDownload loadDocument(Long documentId) {
        EmployeeDocument document = employeeDocumentRepository.findActiveById(documentId)
                .orElseThrow(() -> new BusinessException("Document does not exist"));
        return loadFile(
                document.getStoredFileName(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getFileSize()
        );
    }

    // --- Helper ---

    private Employee findActiveById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Employee not found with id: " + id));
        if (employee.getDeleted()) {
            throw new BusinessException("Employee has been deleted");
        }
        return employee;
    }

    private void mapRequestToEntity(EmployeeRequest request, Employee employee, boolean creating) {
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFullName(request.getFullName());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setCitizenId(request.getCitizenId());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setPersonalEmail(request.getPersonalEmail());
        employee.setAddress(request.getAddress());
        employee.setJoinDate(request.getJoinDate());
        employee.setTerminationDate(request.getTerminationDate());
        employee.setBankAccount(request.getBankAccount());
        employee.setTaxCode(request.getTaxCode());
        employee.setInsuranceNumber(request.getInsuranceNumber());
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactPhone(request.getEmergencyContactPhone());

        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }

        // Gán phòng ban
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("departmentId", "Department does not exist"));
            employee.setDepartment(dept);
        } else {
            employee.setDepartment(null);
        }

        // Gán chức vụ
        if (request.getPositionId() != null) {
            Position pos = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new BusinessException("positionId", "Position does not exist"));
            employee.setPosition(pos);
        } else {
            employee.setPosition(null);
        }

        mapUserAccount(request, employee, creating);
    }

    private void mapUserAccount(EmployeeRequest request, Employee employee, boolean creating) {
        boolean createUserAccount = Boolean.TRUE.equals(request.getCreateUserAccount());

        if (request.getUserId() != null && createUserAccount) {
            throw new BusinessException("userId", "Choose either an existing user account or create a new one");
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new BusinessException("userId", "User account does not exist"));
            if (Boolean.TRUE.equals(user.getDeleted())) {
                throw new BusinessException("userId", "User account has been deleted");
            }
            ensureUserNotLinkedToAnotherEmployee(user, employee);
            employee.setUser(user);
            return;
        }

        if (createUserAccount) {
            if (employee.getUser() != null) {
                throw new BusinessException("createUserAccount", "Employee already has a user account");
            }
            employee.setUser(createUserForEmployee(request));
            return;
        }

        if (creating) {
            employee.setUser(null);
        }
    }

    private User createUserForEmployee(EmployeeRequest request) {
        String username = accountUsername(request);
        String email = accountEmail(request, username);
        String password = accountPassword(request);

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("accountUsername", "Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("accountEmail", "Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(accountRole(request));
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private void ensureUserNotLinkedToAnotherEmployee(User user, Employee employee) {
        employeeRepository.findActiveByUserId(user.getId())
                .filter(existing -> employee.getId() == null || !existing.getId().equals(employee.getId()))
                .ifPresent(existing -> {
                    throw new BusinessException("userId", "User account is already linked to another employee");
                });
    }

    private Role accountRole(EmployeeRequest request) {
        if (request.getAccountRoleId() != null) {
            return roleRepository.findById(request.getAccountRoleId())
                    .orElseThrow(() -> new BusinessException("accountRoleId", "Role does not exist"));
        }

        return roleRepository.findByName(DEFAULT_ACCOUNT_ROLE)
                .orElseThrow(() -> new BusinessException("accountRoleId", "Default Employee role does not exist"));
    }

    private String accountUsername(EmployeeRequest request) {
        String raw = firstNonBlank(request.getAccountUsername(), request.getEmployeeCode());
        String username = raw.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "");

        if (username.isBlank()) {
            throw new BusinessException("accountUsername", "Username is required");
        }

        return username;
    }

    private String accountEmail(EmployeeRequest request, String username) {
        String email = firstNonBlank(request.getAccountEmail(), request.getPersonalEmail());
        if (email != null) {
            return email.trim();
        }

        return username + "@" + DEFAULT_ACCOUNT_EMAIL_DOMAIN;
    }

    private String accountPassword(EmployeeRequest request) {
        String password = firstNonBlank(request.getAccountPassword(), DEFAULT_ACCOUNT_PASSWORD);
        if (password.length() < 6) {
            throw new BusinessException("accountPassword", "Password must be at least 6 characters");
        }
        return password;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private EmployeeDocument storeDocument(Employee employee, MultipartFile file) {
        StoredFile storedFile = storeFile(file);
        EmployeeDocument document = new EmployeeDocument();
        document.setEmployee(employee);
        document.setOriginalFileName(storedFile.originalFileName());
        document.setStoredFileName(storedFile.storedFileName());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        return employeeDocumentRepository.save(document);
    }

    private StoredFile storeFile(MultipartFile file) {
        try {
            Files.createDirectories(employeeUploadRoot);
            String originalFileName = sanitizeFileName(file.getOriginalFilename());
            String storedFileName = UUID.randomUUID() + extensionOf(originalFileName);
            Files.copy(file.getInputStream(), resolveStoredPath(storedFileName), StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(originalFileName, storedFileName);
        } catch (IOException ex) {
            throw new BusinessException("files", "Unable to store uploaded file");
        }
    }

    private EmployeeFileDownload loadFile(String storedFileName, String originalFileName, String contentType, Long fileSize) {
        Path filePath = resolveStoredPath(storedFileName);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("File is not available");
            }
            return new EmployeeFileDownload(resource, originalFileName, contentType, fileSize);
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
        Path target = employeeUploadRoot.resolve(storedFileName).normalize();
        if (!target.startsWith(employeeUploadRoot)) {
            throw new BusinessException("Invalid file path");
        }
        return target;
    }

    private String sanitizeFileName(String fileName) {
        String value = fileName == null || fileName.isBlank()
                ? "employee-file"
                : Paths.get(fileName).getFileName().toString();
        value = value.replaceAll("[^A-Za-z0-9._-]", "_");
        return value.isBlank() ? "employee-file" : value;
    }

    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index);
    }

    private record StoredFile(String originalFileName, String storedFileName) {}

    public record EmployeeFileDownload(
            Resource resource,
            String fileName,
            String contentType,
            Long fileSize
    ) {}
}
