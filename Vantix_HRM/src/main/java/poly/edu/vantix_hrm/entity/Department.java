package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho bảng 'departments' (Phòng ban) trong hệ thống Vantix-HRM.
 * Quản lý thông tin cơ bản của phòng ban, trưởng phòng và các mối quan hệ liên quan.
 */
@Entity
@Table(name = "departments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Department extends BaseEntity {

    /**
     * ID tự tăng, khóa chính của bảng departments.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên phòng ban (Ví dụ: IT, Nhân sự, Kế toán).
     * Ràng buộc: Không được để trống và không được trùng lặp.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Mô tả chi tiết về chức năng hoặc nhiệm vụ của phòng ban.
     */
    private String description;

    /**
     * Trưởng phòng của phòng ban này.
     * Mối quan hệ: 1-1 (Một phòng ban tại một thời điểm có một trưởng phòng).
     * FetchType.LAZY: Chỉ tải dữ liệu Employee khi được gọi đến để tối ưu hiệu năng.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    /**
     * Danh sách tất cả nhân viên thuộc phòng ban này.
     * Mối quan hệ: 1-N (Một phòng ban có nhiều nhân viên).
     * mappedBy = "department": Cho biết quan hệ này được quản lý bởi field 'department' trong Entity Employee.
     */
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    /**
     * Danh sách các vị trí công việc (Position) thuộc phòng ban này quản lý.
     * Mối quan hệ: 1-N (Một phòng ban có nhiều vị trí/chức vụ khác nhau).
     */
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Position> positions = new ArrayList<>();
}