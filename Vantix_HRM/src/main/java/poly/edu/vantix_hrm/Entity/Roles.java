package poly.edu.vantix_hrm.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Roles")
@NoArgsConstructor
@Data
@AllArgsConstructor

public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private Integer roleID;

    @Column(name ="RoleName", nullable = false, unique = true, length = 100)
    private String RoleName;

    @Column(name = "Description", length = 100)
    private String Description;

}
