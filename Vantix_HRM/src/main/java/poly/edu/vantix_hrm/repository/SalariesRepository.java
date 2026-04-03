package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    @Query("SELECT s FROM Salary s " + "JOIN FETCH s.employee e " + "JOIN FETCH e.department d " + "WHERE YEAR(s.salaryMonth) = :year AND MONTH(s.salaryMonth) = :month")
    List<Salary> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    /**
     * Cập nhật trạng thái hàng loạt cho bảng lương theo tháng/năm.
     * Cần có @Modifying cho các lệnh UPDATE/DELETE.
     */
    @Modifying
    @Query("UPDATE Salary s SET s.status = :status WHERE YEAR(s.salaryMonth) = :year AND MONTH(s.salaryMonth) = :month")
    int updateStatusByMonthAndYear(@Param("status") Salary.SalaryStatus status, @Param("month") int month, @Param("year") int year);


}