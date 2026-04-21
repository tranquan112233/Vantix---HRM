package poly.edu.vantix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/*
 * Role
 * - Vai trò trong hệ thống
 * - Role chỉ là nhóm chứa Permission
 * - Không dùng tên Role để kiểm tra quyền trực tiếp
 */
@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    // Tên role, ví dụ: Admin, HR, Employee
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    // Mô tả role
    @Column(name = "description", length = 255)
    private String description;

    // Danh sách quyền thuộc role
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}