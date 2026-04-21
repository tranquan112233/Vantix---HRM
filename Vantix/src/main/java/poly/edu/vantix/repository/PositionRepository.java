package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.Position;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByCode(String code);

    @Query("SELECT p FROM Position p LEFT JOIN FETCH p.department " +
           "WHERE p.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:departmentId IS NULL OR p.department.id = :departmentId) " +
           "ORDER BY p.id DESC")
    List<Position> search(@Param("keyword") String keyword, @Param("departmentId") Long departmentId);

    @Query(
            value = "SELECT p FROM Position p LEFT JOIN FETCH p.department " +
                    "WHERE p.deleted = false " +
                    "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "AND (:departmentId IS NULL OR p.department.id = :departmentId) " +
                    "ORDER BY p.id DESC",
            countQuery = "SELECT COUNT(p) FROM Position p " +
                    "WHERE p.deleted = false " +
                    "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "AND (:departmentId IS NULL OR p.department.id = :departmentId)"
    )
    Page<Position> search(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );

    long countByDeletedFalse();

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByNameAndDeletedFalse(String name);
}
