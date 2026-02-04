package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Positions") // Công dụng: Chức vụ trong công ty
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Positions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Integer positionId; // ID chức vụ

    @Column(name = "position_name", length = 100, unique = true)
    private String positionName; // Tên chức vụ
}