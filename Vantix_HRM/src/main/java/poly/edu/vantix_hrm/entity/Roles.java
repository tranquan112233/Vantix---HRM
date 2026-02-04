package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Roles") // Công dụng: Lưu vai trò người dùng (Admin, HR, Employee)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId; // ID vai trò

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName; // Tên vai trò

    @Column(name = "description")
    private String description; // Mô tả vai trò
}