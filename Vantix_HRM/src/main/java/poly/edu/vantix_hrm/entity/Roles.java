package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name ="RoleName")
    private String roleName;

    @Column(name = "Description")
    private String description;

}
