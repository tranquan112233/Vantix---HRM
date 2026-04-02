package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_type_id")
    private Long leaveTypeId;

    @Column(name = "type_name", length = 50)
    private String typeName;

    @Column(name = "is_paid")
    private Boolean isPaid;
}