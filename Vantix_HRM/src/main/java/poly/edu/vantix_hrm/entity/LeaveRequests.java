package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Leave_Requests") // Công dụng: Đơn xin nghỉ phép
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_id")
    private Integer leaveId; // ID đơn nghỉ

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employees employee; // Nhân viên gửi đơn

    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveTypes leaveType; // Loại nghỉ

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // Ngày bắt đầu nghỉ

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // Ngày kết thúc nghỉ

    @Column(name = "total_shift", nullable = false)
    private Integer totalShift; // Tổng số ca

    @Column(name = "reason", length = 255)
    private String reason; // Lý do nghỉ

    @Column(name = "status")
    private LeaveRequestsStatus status = LeaveRequestsStatus.PENDING; // Trạng thái

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Employees approvedBy; // Người duyệt

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // Thời gian tạo

    public enum LeaveRequestsStatus {PENDING, APPROVED, REJECTED}
}