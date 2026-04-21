package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Department;
import poly.edu.vantix.entity.Employee;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Long headEmployeeId;
    private String headEmployeeCode;
    private String headEmployeeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DepartmentResponse fromEntity(Department d) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .code(d.getCode())
                .name(d.getName())
                .description(d.getDescription())
                .headEmployeeId(d.getHeadEmployeeId())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    public static DepartmentResponse fromEntity(Department d, Employee headEmployee) {
        DepartmentResponse response = fromEntity(d);
        if (headEmployee != null) {
            response.setHeadEmployeeId(headEmployee.getId());
            response.setHeadEmployeeCode(headEmployee.getEmployeeCode());
            response.setHeadEmployeeName(headEmployee.getFullName());
        }
        return response;
    }
}
