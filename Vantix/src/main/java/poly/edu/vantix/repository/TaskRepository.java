package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.WorkTask;
import poly.edu.vantix.entity.enums.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<WorkTask, Long> {

    @Query("SELECT DISTINCT t FROM WorkTask t " +
           "LEFT JOIN FETCH t.assignee a " +
           "LEFT JOIN FETCH a.department " +
           "LEFT JOIN FETCH a.position " +
           "WHERE t.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
           "ORDER BY t.id DESC")
    List<WorkTask> search(
            @Param("keyword") String keyword,
            @Param("status") TaskStatus status,
            @Param("assigneeId") Long assigneeId
    );

    @Query("SELECT DISTINCT t FROM WorkTask t " +
           "LEFT JOIN FETCH t.assignee a " +
           "LEFT JOIN FETCH a.department " +
           "LEFT JOIN FETCH a.position " +
           "WHERE t.id = :id AND t.deleted = false")
    Optional<WorkTask> findActiveById(@Param("id") Long id);

    boolean existsByDeletedFalse();

    @Query("SELECT t FROM WorkTask t " +
           "JOIN FETCH t.assignee a " +
           "JOIN FETCH a.user u " +
           "WHERE t.deleted = false " +
           "AND t.status <> poly.edu.vantix.entity.enums.TaskStatus.DONE " +
           "AND t.dueDate IS NOT NULL " +
           "AND t.dueDate <= :today " +
           "AND (t.lastOverdueNotifiedAt IS NULL OR t.lastOverdueNotifiedAt < :today) " +
           "AND u.deleted = false")
    List<WorkTask> findTasksNeedingOverdueNotification(@Param("today") LocalDate today);
}
