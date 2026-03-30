package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/*
 * Role — vai trò trong hệ thống
 *
 * Ví dụ: ADMIN, MANAGER, EMPLOYEE
 *
 * Quan hệ:
 *   Role → User       : 1 Role có nhiều User
 *   Role ←→ Permission : nhiều-nhiều
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên role — ví dụ: ADMIN, MANAGER, EMPLOYEE
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // Danh sách quyền của role này
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}