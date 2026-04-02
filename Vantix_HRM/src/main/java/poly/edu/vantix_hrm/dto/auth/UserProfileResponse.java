package poly.edu.vantix_hrm.dto.auth;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;

    private Long employeeId;

    private String username;

    private String email;

    private String role;

    private List<String> permissions;

}