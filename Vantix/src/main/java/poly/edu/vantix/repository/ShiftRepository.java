package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.Shift;

import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    @Query("SELECT s FROM Shift s WHERE s.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY s.startTime ASC")
    List<Shift> search(@Param("keyword") String keyword);

    @Query("SELECT s FROM Shift s WHERE s.id = :id AND s.deleted = false")
    Optional<Shift> findActiveById(@Param("id") Long id);

    Optional<Shift> findByCode(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByDeletedFalse();
}
