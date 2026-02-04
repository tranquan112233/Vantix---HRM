package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ContractAnnexes") // Công dụng: Bảng phụ lục hợp đồng
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractAnnexes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "annex_id")
    private Integer annexId; // Mã phụ lục

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private Contracts contract; // Hợp đồng gốc

    @Column(name = "effective_date")
    private LocalDate effectiveDate; // Ngày hiệu lực

    @Column(name = "new_salary", precision = 18, scale = 2)
    private BigDecimal newSalary; // Lương mới

    @Column(name = "new_positions", length = 100)
    private String newPositions; // Chức vụ mới

    @Column(name = "content")
    private String content; // Nội dung

    @Column(name = "is_active")
    private boolean isActive; // Kích hoạt
}