package poly.edu.vantix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*
 * Department
 * - Phòng ban trong công ty
 * - Tạm lưu headEmployeeId để tránh vòng lặp entity phức tạp
 */
@Getter
@Setter
@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    // Mã phòng ban
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    // Tên phòng ban
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    // Mô tả phòng ban
    @Column(name = "description", length = 255)
    private String description;

    // Id nhân viên làm trưởng phòng
    @Column(name = "head_employee_id")
    private Long headEmployeeId;
}