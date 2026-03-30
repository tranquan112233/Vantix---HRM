package poly.edu.vantix_hrm.dto.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionRequestDTO {

    @NotBlank(message = "Permission name is required")
    private String name;

    private String description;
}