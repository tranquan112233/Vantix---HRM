package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ContractAnnexes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractAnnexes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AnnexID")
    private Integer annexID;

    @ManyToOne
    @JoinColumn(name = "ContractID", nullable = false)
    private Contracts contract;

    @Column(name = "AnnexCode", length = 50)
    private String annexCode;

    @Column(name = "EffectiveDate", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "NewSalary", precision = 18, scale = 2)
    private BigDecimal newSalary;

    @Column(name = "NewPositions", length = 100)
    private String newPositions;

    @Column(name = "Content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "IsActive")
    private Boolean isActive;
}
