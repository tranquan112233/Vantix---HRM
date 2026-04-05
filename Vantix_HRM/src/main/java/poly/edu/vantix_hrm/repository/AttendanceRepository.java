package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Attendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate BETWEEN :startDate AND :endDate")
    List<Attendance> getMonthlyAttendance(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) > 0 FROM Attendance a " + "WHERE a.employee.id = :employeeId " + "AND a.shift.shiftId = :shiftId " + "AND a.workDate = :workDate")
    Boolean isAlreadyCheckedIn(@Param("employeeId") Long employeeId, @Param("shiftId") Long shiftId, @Param("workDate") LocalDate workDate);

    @Query(value = "SELECT * FROM Attendance " + "WHERE employee_id = :employeeId " + "AND shift_id = :shiftId " + "AND work_date = :workDate " + "LIMIT 1", nativeQuery = true)
    Optional<Attendance> findExistingAttendance(@Param("employeeId") Long employeeId, @Param("shiftId") Long shiftId, @Param("workDate") LocalDate workDate);

    @Query("SELECT a FROM Attendance a " + "WHERE a.workDate = :today " + "AND a.checkOut IS NULL " + "AND a.status = 'DRAFT' " + "AND a.shift.endTime <= :now")
    List<Attendance> findLateEmployees(@Param("today") LocalDate today, @Param("now") LocalTime now);

    @Query(value = "SELECT * FROM Attendance " + "WHERE employee_id = :employeeId " + "AND work_date = :workDate " + "AND status = 'PENDING' " + "LIMIT 1", nativeQuery = true)
    Optional<Attendance> findPendingAttendance(@Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate);

    @Query("SELECT a FROM Attendance a " + "WHERE a.status = 'PENDING' " + "AND a.workDate = :today " + "AND a.shift.endTime < :cutoffTime")
    List<Attendance> findExpiredPending(@Param("today") LocalDate today, @Param("cutoffTime") LocalTime cutoffTime);

    // Dùng cho Attendance Management
    @Query("SELECT a FROM Attendance a " + "JOIN FETCH a.employee e " + "JOIN FETCH a.shift s " + "WHERE e.department.id = :departmentId " + "AND a.status = 'REJECTED' " + "AND a.workDate BETWEEN :startDate AND :endDate " + "ORDER BY e.id ASC, a.workDate DESC")
    List<Attendance> findRejectedByDepartmentAndDateRange(@Param("departmentId") Long departmentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Attendance a " + "JOIN FETCH a.employee e " + "JOIN FETCH a.shift s " + "WHERE e.department.id = :departmentId " + "AND a.status = 'PENDING' " + "AND a.workDate BETWEEN :startDate AND :endDate " + "ORDER BY e.id ASC, a.workDate DESC")
    List<Attendance> findPendingByDepartmentAndDateRange(@Param("departmentId") Long departmentId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :empId AND MONTH(a.workDate) = :month AND YEAR(a.workDate) = :year")
    List<Attendance> findByEmployeeIdAndMonthAndYear(@Param("empId") Long empId, @Param("month") int month, @Param("year") int year);
}