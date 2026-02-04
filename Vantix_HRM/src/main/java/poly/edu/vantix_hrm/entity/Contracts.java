package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Contracts") // Công dụng: Lưu lịch sử hợp đồng lao động
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contracts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Integer contractId; // ID hợp đồng

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employees employee; // Nhân viên ký hợp đồng

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type")
    private Type type; // Loại hợp đồng

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // Ngày bắt đầu

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // Ngày kết thúc

    @Column(name = "position")
    private String position; // Chức vụ tại thời điểm ký

    @Column(name = "base_salary")
    private BigDecimal baseSalary; // Lương cơ bản theo hợp đồng

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ContractStatus status; // Trạng thái hợp đồng

    public enum Type {YEAR_1, YEAR_3, INDEFINITE}
    public enum ContractStatus { ACTIVE, EXPIRED }
}