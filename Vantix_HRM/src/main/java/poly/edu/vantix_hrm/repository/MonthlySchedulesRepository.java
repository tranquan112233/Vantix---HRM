package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.MonthlySchedules;

@Repository
public interface MonthlySchedulesRepository extends JpaRepository<MonthlySchedules, Integer> {
    MonthlySchedules findByEmployee_EmployeeIdAndMonthAndYear(Integer employeeId, Integer month, Integer year);
}
