package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.WorkLocation;

import java.util.List;
import java.util.Optional;

public interface WorkLocationRepository extends JpaRepository<WorkLocation, Long> {

    @Query("SELECT l FROM WorkLocation l WHERE l.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(l.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY l.id DESC")
    List<WorkLocation> search(@Param("keyword") String keyword);

    @Query("SELECT l FROM WorkLocation l WHERE l.id = :id AND l.deleted = false")
    Optional<WorkLocation> findActiveById(@Param("id") Long id);

    boolean existsByDeletedFalse();
}
