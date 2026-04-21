package poly.edu.vantix.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.SystemLog;
import poly.edu.vantix.entity.enums.LogLevel;

import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    @Query(
            value = "SELECT l FROM SystemLog l WHERE l.deleted = false " +
                    "AND (:level IS NULL OR l.level = :level) " +
                    "AND (:module IS NULL OR LOWER(l.module) = LOWER(:module)) " +
                    "AND (:keyword IS NULL OR " +
                    "     LOWER(l.action) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(l.module) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(l.entityName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(l.actorUsername) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "ORDER BY l.createdAt DESC, l.id DESC",
            countQuery = "SELECT COUNT(l) FROM SystemLog l WHERE l.deleted = false " +
                    "AND (:level IS NULL OR l.level = :level) " +
                    "AND (:module IS NULL OR LOWER(l.module) = LOWER(:module)) " +
                    "AND (:keyword IS NULL OR " +
                    "     LOWER(l.action) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(l.module) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(l.entityName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(l.actorUsername) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "     OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')))"
    )
    Page<SystemLog> search(
            @Param("keyword") String keyword,
            @Param("level") LogLevel level,
            @Param("module") String module,
            Pageable pageable
    );

    @Query("SELECT DISTINCT l.module FROM SystemLog l " +
           "WHERE l.deleted = false AND l.module IS NOT NULL AND l.module <> '' " +
           "ORDER BY l.module")
    List<String> findModules();

    long countByDeletedFalse();
}
