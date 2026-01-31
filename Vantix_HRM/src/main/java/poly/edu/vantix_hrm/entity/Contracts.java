package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contracts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ContractID")
    private Integer contractID;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "ContractCode", length = 50)
    private String contractCode;

    @Column(name = "ContractType", length = 50)
    private String contractType;

    @Column(name = "Positions", length = 100)
    private String positions;

    @Column(name = "Salary", precision = 18, scale = 2)
    private BigDecimal salary;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "Status", length = 20)
    private String status = "ACTIVE";
}
