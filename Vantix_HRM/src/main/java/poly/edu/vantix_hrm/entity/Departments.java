package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Departments") // Công dụng: Quản lý phòng ban
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Departments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Integer departmentId; // ID phòng ban

    @Column(name = "department_name", length = 100, nullable = false)
    private String departmentName; // Tên phòng ban

    @Column(name = "description", length = 255)
    private String description; // Mô tả chức năng
}