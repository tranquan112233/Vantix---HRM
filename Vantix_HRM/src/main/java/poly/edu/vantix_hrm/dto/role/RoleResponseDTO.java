package poly.edu.vantix_hrm.dto.role;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponseDTO {

    private Long id;
    private String name;
    private String description;

    // Danh sách tên Permission thuộc Role
    private Set<String> permissions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}