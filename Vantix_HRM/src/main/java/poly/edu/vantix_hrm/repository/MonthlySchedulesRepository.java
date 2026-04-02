package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.MonthlySchedules;

@Repository
public interface MonthlySchedulesRepository extends JpaRepository<MonthlySchedules, Long> {
    @Query("SELECT ms FROM MonthlySchedules ms WHERE ms.employee.id = :employeeId AND ms.month = :month AND ms.year = :year")
    MonthlySchedules findByEmployee_IdAndMonthAndYear(@Param("employeeId") Long employeeId,
                                                       @Param("month") Integer month,
                                                              @Param("year") Integer year);

}
