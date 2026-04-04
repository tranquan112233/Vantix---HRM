package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.PayrollBatch;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollBatchRepository extends JpaRepository<PayrollBatch, Integer> {

    // Tìm kiếm đợt lương theo tháng và năm
    @Query("SELECT p FROM PayrollBatch p WHERE YEAR(p.salaryMonth) = :year AND MONTH(p.salaryMonth) = :month")
    Optional<PayrollBatch> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Lấy tất cả các đợt chốt lương, sắp xếp mới nhất lên đầu (Dùng Query rõ ràng)
    @Query("SELECT p FROM PayrollBatch p ORDER BY p.salaryMonth DESC")
    List<PayrollBatch> findAllBatchesDesc();
}