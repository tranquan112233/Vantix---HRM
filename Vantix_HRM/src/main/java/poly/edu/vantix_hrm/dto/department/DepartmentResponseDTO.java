package poly.edu.vantix_hrm.dto.department;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponseDTO {

    private Long id;
    private String name;
    private String description;

    // Thông tin trưởng phòng
    private Long managerId;
    private String managerName;

    // Thống kê
    private Integer employeeCount;
    private Integer positionCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}