package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.DailyWorkSchedules;

import java.util.List;

@Repository
public interface DailyWorkSchedulesRepository extends JpaRepository<DailyWorkSchedules, Integer> {
    List<DailyWorkSchedules> findByMonthlySchedule_MonthlyScheduleId(Integer monthlyScheduleId);
}
