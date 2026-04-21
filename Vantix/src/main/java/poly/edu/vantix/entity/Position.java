package poly.edu.vantix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/*
 * Position
 * - Chức vụ của nhân viên
 * - Ví dụ: Intern, Staff, Team Leader, Manager
 */
@Getter
@Setter
@Entity
@Table(name = "positions")
public class Position extends BaseEntity {

    // Mã chức vụ
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    // Tên chức vụ
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    // Mô tả chức vụ
    @Column(name = "description", length = 255)
    private String description;

    // Chức vụ có thể thuộc một phòng ban
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}