package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.TaskAttachment;

import java.util.Optional;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    @Query("SELECT a FROM TaskAttachment a JOIN FETCH a.task " +
           "WHERE a.id = :id AND a.deleted = false")
    Optional<TaskAttachment> findActiveById(@Param("id") Long id);

    @Query("SELECT a FROM TaskAttachment a JOIN FETCH a.task " +
           "WHERE a.id = :id AND a.task.id = :taskId AND a.deleted = false")
    Optional<TaskAttachment> findActiveByIdAndTaskId(
            @Param("id") Long id,
            @Param("taskId") Long taskId
    );
}
