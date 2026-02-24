package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Entity
@Table(name = "Shifts") // Công dụng: Ca làm việc
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shifts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_id")
    private Integer shiftId; // ID ca làm

    @Column(name = "shift_name", length = 50)
    private String shiftName; // Tên ca

    @Column(name = "start_time")
    private LocalTime startTime; // Giờ bắt đầu

    @Column(name = "end_time")
    private LocalTime endTime; // Giờ kết thúc
}