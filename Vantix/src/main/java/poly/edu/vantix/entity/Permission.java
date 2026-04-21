package poly.edu.vantix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*
 * Permission
 * - Quyền chi tiết dùng để phân quyền thật
 * - Ví dụ: USER_VIEW, USER_CREATE, ROLE_UPDATE
 */
@Getter
@Setter
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    // Tên quyền, duy nhất trong hệ thống
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    // Mô tả quyền
    @Column(name = "description", length = 255)
    private String description;
}