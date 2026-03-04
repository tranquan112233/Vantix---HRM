package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Leave_Types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_type_id")
    private Integer leaveTypeId;

    @Column(name = "type_name", length = 50)
    private String typeName;

    @Column(name = "is_paid")
    private Boolean isPaid;
}