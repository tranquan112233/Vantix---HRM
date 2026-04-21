package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix.entity.PublicHoliday;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {

    Optional<PublicHoliday> findByHolidayDateAndDeletedFalse(LocalDate holidayDate);

    boolean existsByHolidayDateAndDeletedFalse(LocalDate holidayDate);

    List<PublicHoliday> findByHolidayDateBetweenAndDeletedFalse(LocalDate from, LocalDate to);
}
