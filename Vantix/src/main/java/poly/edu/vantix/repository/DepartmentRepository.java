package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCode(String code);

    @Query("SELECT d FROM Department d WHERE d.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY d.id DESC")
    List<Department> search(@Param("keyword") String keyword);

    @Query("SELECT d FROM Department d WHERE d.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY d.id DESC")
    Page<Department> search(@Param("keyword") String keyword, Pageable pageable);

    long countByDeletedFalse();

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByNameAndDeletedFalse(String name);
}
