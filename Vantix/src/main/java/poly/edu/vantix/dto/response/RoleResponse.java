package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Permission;
import poly.edu.vantix.entity.Role;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Long id;
    private String name;
    private String description;
    private List<PermissionInfo> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionInfo {
        private Long id;
        private String name;
        private String description;
    }

    public static RoleResponse fromEntity(Role r) {
        List<PermissionInfo> perms = r.getPermissions() == null
                ? List.of()
                : r.getPermissions().stream()
                        .map(p -> PermissionInfo.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .description(p.getDescription())
                                .build())
                        .toList();

        return RoleResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .permissions(perms)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
