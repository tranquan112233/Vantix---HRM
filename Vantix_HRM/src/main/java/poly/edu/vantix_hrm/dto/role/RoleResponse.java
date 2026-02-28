package poly.edu.vantix_hrm.dto.role;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleResponse {

    private Integer id;
    private String roleName;
    private String description;
}