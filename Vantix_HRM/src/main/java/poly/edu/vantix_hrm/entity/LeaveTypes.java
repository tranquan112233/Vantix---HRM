package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Leave_Types") // Công dụng: Loại nghỉ phép
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveTypes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_type_id")
    private Integer leaveTypeId; // ID loại nghỉ

    @Column(name = "type_name", length = 50)
    private String typeName; // Tên loại nghỉ

    @Column(name = "is_paid")
    private Boolean isPaid; // Có hưởng lương hay không
}