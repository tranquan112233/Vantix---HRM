package poly.edu.vantix_hrm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/*
 * Permission — quyền cụ thể trong hệ thống
 *
 * Ví dụ: USER_CREATE, USER_UPDATE, EMPLOYEE_VIEW
 *
 * Quan hệ:
 *   Permission ←→ Role : nhiều-nhiều (mappedBy bên Role)
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên quyền — ví dụ: USER_CREATE, EMPLOYEE_VIEW
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // Các Role có permission này (không quản lý quan hệ từ phía này)
    @JsonIgnoreProperties("permissions") // Ngăn không cho quét ngược lại thuộc tính permissions của Role
    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}