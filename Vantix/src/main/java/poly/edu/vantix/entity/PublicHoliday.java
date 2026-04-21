package poly.edu.vantix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "public_holidays")
public class PublicHoliday extends BaseEntity {

    @Column(name = "holiday_date", nullable = false, unique = true)
    private LocalDate holidayDate;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "paid_day", nullable = false)
    private Boolean paidDay = true;
}
