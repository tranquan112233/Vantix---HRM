package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.PayrollPeriod;

import java.util.List;
import java.util.Optional;

public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {

    @Query("SELECT p FROM PayrollPeriod p " +
           "LEFT JOIN FETCH p.approvedBy " +
           "WHERE p.deleted = false AND p.id = :id")
    Optional<PayrollPeriod> findActiveById(@Param("id") Long id);

    Optional<PayrollPeriod> findByYearAndMonthAndDeletedFalse(Integer year, Integer month);

    Optional<PayrollPeriod> findByYearAndMonth(Integer year, Integer month);

    @Query("SELECT p FROM PayrollPeriod p " +
           "LEFT JOIN FETCH p.approvedBy " +
           "WHERE p.deleted = false " +
           "AND (:year IS NULL OR p.year = :year) " +
           "ORDER BY p.year DESC, p.month DESC")
    List<PayrollPeriod> findAllByYear(@Param("year") Integer year);
}
