package poly.edu.vantix_hrm.dto.position;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionResponseDTO {

    private Long id;
    private String name;
    private String description;

    // Thông tin phòng ban
    private Long departmentId;
    private String departmentName;

    // Thống kê
    private Integer employeeCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}