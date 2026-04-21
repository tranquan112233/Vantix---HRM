package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Position;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Long departmentId;
    private String departmentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PositionResponse fromEntity(Position p) {
        return PositionResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .departmentId(p.getDepartment() != null ? p.getDepartment().getId() : null)
                .departmentName(p.getDepartment() != null ? p.getDepartment().getName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
