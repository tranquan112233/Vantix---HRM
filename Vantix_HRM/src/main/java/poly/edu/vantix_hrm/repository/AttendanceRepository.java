package poly.edu.vantix_hrm.dao;

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
public interface AttendanceDAO extends JpaRepository<Attendance, Integer> {
    @Query(value = "SELECT * FROM Attendance " + "WHERE employee_id = :employeeId " + "AND MONTH(work_date) = :month " + "AND YEAR(work_date) = :year", nativeQuery = true)
    List<Attendance> getMonthlyAttendance(@Param("employeeId") Integer employeeId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(a) > 0 FROM Attendance a " + "WHERE a.employee.employeeId = :employeeId " + "AND a.shift.shiftId = :shiftId " + "AND a.workDate = :workDate")
    Boolean isAlreadyCheckedIn(@Param("employeeId") Integer employeeId, @Param("shiftId") Integer shiftId, @Param("workDate") LocalDate workDate);

    @Query(value = "SELECT * FROM Attendance " + "WHERE employee_id = :employeeId " + "AND shift_id = :shiftId " + "AND work_date = :workDate " + "LIMIT 1", nativeQuery = true)
    Optional<Attendance> findExistingAttendance(@Param("employeeId") Integer employeeId, @Param("shiftId") Integer shiftId, @Param("workDate") LocalDate workDate);

    @Query("SELECT a FROM Attendance a " + "WHERE a.workDate = :today " + "AND a.checkOut IS NULL " + "AND a.status = 'DRAFT' " + "AND a.shift.endTime < :now")
    List<Attendance> findLateEmployees(@Param("today") LocalDate today, @Param("now") LocalTime now);

    @Query(value = "SELECT * FROM Attendance " + "WHERE employee_id = :employeeId " + "AND work_date = :workDate " + "AND status = 'PENDING' " + "LIMIT 1", nativeQuery = true)
    Optional<Attendance> findPendingAttendance(@Param("employeeId") Integer employeeId, @Param("workDate") LocalDate workDate);

    @Query("SELECT a FROM Attendance a " + "WHERE a.status = 'PENDING' " + "AND a.workDate = :today " + "AND a.shift.endTime < :cutoffTime")
    List<Attendance> findExpiredPending(@Param("today") LocalDate today, @Param("cutoffTime") LocalTime cutoffTime);
}
