package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "positions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Integer id;

    @Column(name = "position_name", nullable = false, length = 100)
    private String name;
}

