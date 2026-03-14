package poly.edu.vantix_hrm.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserResponse {

    private Integer userId;
    private String username;
    private String email;

    // Trả về danh sách ID quyền để Frontend bind vào ô select/checkbox lúc Edit
    private Integer roleId;

    // Trả về danh sách tên quyền để Frontend hiển thị lên Table cho đẹp
    private String roleName;

    private String status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    private List<String> permissions;

}