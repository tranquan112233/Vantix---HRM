package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// Công dụng: Lưu vai trò người dùng (Admin, HR, Employee)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId; // ID vai trò

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName; // Tên vai trò

    @Column(name = "description", length = 255)
    private String description; // Mô tả vai trò (Chứa các thông tin như quyền truy cập, v.v)
}
