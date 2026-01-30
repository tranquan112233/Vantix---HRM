package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "Departments")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Departments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DepartmentID")
    private Integer departmentID;

    @Column(name = "DepartmentName")
    private String departmentName;

    @Column(name = "Description")
    private String description;
}
