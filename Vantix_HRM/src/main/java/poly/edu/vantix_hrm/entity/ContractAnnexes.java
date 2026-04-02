package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contract_annexes") // Đã sửa tên bảng cho khớp với SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractAnnexes extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "annex_id")
    private Long annexId; // Mã phụ lục

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract; // Hợp đồng gốc (Foreign Key)

    @Column(name = "effective_date")
    private LocalDate effectiveDate; // Ngày hiệu lực

    @Column(name = "new_salary", precision = 18, scale = 2)
    private BigDecimal newSalary; // Lương mới

    @Column(name = "new_positions", length = 100)
    private String newPositions; // Chức vụ mới

    @Column(name = "content")
    private String content; // Nội dung

    @Column(name = "is_active")
    private Boolean isActive;
}