package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.Salary;

import java.util.List;

@Repository
public interface SalariesRepository extends JpaRepository<Salary, Integer> {

    /**
     * Tìm danh sách bảng lương theo tháng và năm.
     *
     * @param month Tháng cần tìm (1 - 12)
     * @param year  Năm cần tìm (VD: 2026)
     * @return Danh sách Salary
     */
    @Query(value = "SELECT * FROM salaries WHERE YEAR(salary_month) = :year AND MONTH(salary_month) = :month", nativeQuery = true)
    List<Salary> findByMonthAndYearNative(@Param("month") int month, @Param("year") int year);

}