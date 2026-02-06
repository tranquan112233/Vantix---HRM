package poly.edu.vantix_hrm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Attendance;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceDAO extends JpaRepository<Attendance, Integer> {
    @Query(value = "SELECT * FROM Attendance " + "WHERE employee_id = :employeeId " + "AND MONTH(work_date) = :month " + "AND YEAR(work_date) = :year", nativeQuery = true)
    List<Attendance> getMonthlyAttendance(@Param("employeeId") Integer employeeId, @Param("month") int month, @Param("year") int year);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM Attendance " + "WHERE employee_id = :employeeId " + "AND shift_id = :shiftId " + "AND work_date = :workDate)", nativeQuery = true)
    Boolean isAlreadyCheckedIn(@Param("employeeId") Integer employeeId, @Param("shiftId") Integer shiftId, @Param("workDate") LocalDate workDate);
}
