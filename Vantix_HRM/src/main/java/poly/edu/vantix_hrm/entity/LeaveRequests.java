package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "LeaveRequests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LeaveID")
    private Integer leaveID;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "ApprovedBy")
    private Users approvedBy;

    @Column(name = "LeaveType", length = 50)
    private String leaveType;

    @Column(name = "FromDate", nullable = false)
    private LocalDate fromDate;

    @Column(name = "ToDate", nullable = false)
    private LocalDate toDate;

    @Column(name = "TotalDays",precision = 4, scale = 1, nullable = false)
    private BigDecimal totalDays;

    @Column(name = "Reason", length = 255)
    private String reason;

    public enum Status {
        Pending,
        Approved,
        Rejected
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private Status status = Status.Pending;

    @Column(name = "RejectionReason")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

}


