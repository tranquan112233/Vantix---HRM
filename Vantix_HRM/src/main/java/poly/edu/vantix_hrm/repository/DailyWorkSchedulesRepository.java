package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.DailyWorkSchedules;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyWorkSchedulesRepository extends JpaRepository<DailyWorkSchedules, Long> {
    // Dùng cho Schedules
    List<DailyWorkSchedules> findByMonthlySchedule_MonthlyScheduleId(Long monthlyScheduleId);

    // Dùng cho Attendance
    @Query("SELECT dws FROM DailyWorkSchedules dws " + "WHERE dws.monthlySchedule.employee.id = :employeeId " + "AND dws.workDate = :workDate")
    Optional<DailyWorkSchedules> findByEmployeeIdAndWorkDate(@Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate);
}
