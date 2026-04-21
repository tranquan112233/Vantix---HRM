package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.WorkSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    @Query("SELECT s FROM WorkSchedule s " +
           "JOIN FETCH s.employee e " +
           "LEFT JOIN FETCH e.department " +
           "JOIN FETCH s.shift " +
           "LEFT JOIN FETCH s.location " +
           "WHERE s.deleted = false " +
           "AND (:from IS NULL OR s.workDate >= :from) " +
           "AND (:to IS NULL OR s.workDate <= :to) " +
           "AND (:employeeId IS NULL OR s.employee.id = :employeeId) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "ORDER BY s.workDate DESC, s.shift.startTime ASC")
    List<WorkSchedule> search(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId
    );

    @Query("SELECT s FROM WorkSchedule s " +
           "JOIN FETCH s.employee e " +
           "JOIN FETCH s.shift " +
           "LEFT JOIN FETCH s.location " +
           "WHERE s.id = :id AND s.deleted = false")
    Optional<WorkSchedule> findActiveById(@Param("id") Long id);

    @Query("SELECT s FROM WorkSchedule s " +
           "JOIN FETCH s.shift " +
           "LEFT JOIN FETCH s.location " +
           "WHERE s.deleted = false " +
           "AND s.employee.id = :employeeId " +
           "AND s.workDate = :date")
    Optional<WorkSchedule> findByEmployeeAndDate(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date
    );

    boolean existsByEmployeeIdAndWorkDateAndDeletedFalse(Long employeeId, LocalDate workDate);
}
