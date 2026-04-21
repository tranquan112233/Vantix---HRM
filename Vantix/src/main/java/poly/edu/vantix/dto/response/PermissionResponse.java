package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Permission;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private Long id;
    private String name;
    private String description;

    public static PermissionResponse fromEntity(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .build();
    }
}
