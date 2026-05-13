package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.enums.EmploymentStatus;
import poly.edu.vantix.entity.enums.Gender;
import poly.edu.vantix.entity.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String employeeCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String citizenId;
    private String phoneNumber;
    private String personalEmail;
    private String photoOriginalFileName;
    private String photoContentType;
    private Long photoFileSize;
    private String photoUrl;
    private String address;

    private Long departmentId;
    private String departmentName;

    private Long positionId;
    private String positionName;

    private Long userId;
    private String accountUsername;
    private String accountEmail;
    private String accountRoleName;
    private UserStatus accountStatus;

    private LocalDate joinDate;
    private LocalDate terminationDate;
    private EmploymentStatus status;

    private String bankAccount;
    private String taxCode;
    private String insuranceNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<EmployeeDocumentResponse> documents;

    public static EmployeeResponse fromEntity(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .employeeCode(e.getEmployeeCode())
                .fullName(e.getFullName())
                .dateOfBirth(e.getDateOfBirth())
                .gender(e.getGender())
                .citizenId(e.getCitizenId())
                .phoneNumber(e.getPhoneNumber())
                .personalEmail(e.getPersonalEmail())
                .photoOriginalFileName(e.getPhotoOriginalFileName())
                .photoContentType(e.getPhotoContentType())
                .photoFileSize(e.getPhotoFileSize())
                .photoUrl(e.getPhotoFileName() != null ? "/api/employees/" + e.getId() + "/photo" : null)
                .address(e.getAddress())
                .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .positionId(e.getPosition() != null ? e.getPosition().getId() : null)
                .positionName(e.getPosition() != null ? e.getPosition().getName() : null)
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .accountUsername(e.getUser() != null ? e.getUser().getUsername() : null)
                .accountEmail(e.getUser() != null ? e.getUser().getEmail() : null)
                .accountRoleName(e.getUser() != null && e.getUser().getRole() != null ? e.getUser().getRole().getName() : null)
                .accountStatus(e.getUser() != null ? e.getUser().getStatus() : null)
                .joinDate(e.getJoinDate())
                .terminationDate(e.getTerminationDate())
                .status(e.getStatus())
                .bankAccount(e.getBankAccount())
                .taxCode(e.getTaxCode())
                .insuranceNumber(e.getInsuranceNumber())
                .emergencyContactName(e.getEmergencyContactName())
                .emergencyContactPhone(e.getEmergencyContactPhone())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .documents(e.getDocuments() == null ? List.of() : e.getDocuments().stream()
                        .filter(document -> !Boolean.TRUE.equals(document.getDeleted()))
                        .map(EmployeeDocumentResponse::fromEntity)
                        .toList())
                .build();
    }
}
