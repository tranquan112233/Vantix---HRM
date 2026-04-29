package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH a.schedule s " +
           "LEFT JOIN FETCH s.shift " +
           "LEFT JOIN FETCH s.location " +
           "WHERE a.deleted = false " +
           "AND (:from IS NULL OR a.workDate >= :from) " +
           "AND (:to IS NULL OR a.workDate <= :to) " +
           "AND (:employeeId IS NULL OR a.employee.id = :employeeId) " +
           "ORDER BY a.workDate DESC, a.checkInAt DESC")
    List<Attendance> search(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("employeeId") Long employeeId
    );

    @Query("SELECT a FROM Attendance a " +
           "LEFT JOIN FETCH a.schedule s " +
           "LEFT JOIN FETCH s.shift " +
           "LEFT JOIN FETCH s.location " +
           "WHERE a.deleted = false " +
           "AND a.employee.id = :employeeId " +
           "AND a.workDate = :date")
    Optional<Attendance> findByEmployeeAndDate(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date
    );

    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.employee e " +
           "LEFT JOIN FETCH e.user " +
           "LEFT JOIN FETCH a.schedule s " +
           "LEFT JOIN FETCH s.shift " +
           "WHERE a.deleted = false " +
           "AND a.workDate = :workDate " +
           "AND a.checkInAt IS NOT NULL " +
           "AND a.checkOutAt IS NULL " +
           "AND a.status <> poly.edu.vantix.entity.enums.AttendanceStatus.MISSING_CHECKOUT")
    List<Attendance> findOpenAttendancesOn(@Param("workDate") LocalDate workDate);
}
