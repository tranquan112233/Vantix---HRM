package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.Notification;
import poly.edu.vantix.entity.enums.NotificationStatus;
import poly.edu.vantix.entity.enums.NotificationType;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.deleted = false " +
           "AND n.user.id = :userId " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByUserId(@Param("userId") Long userId);

    @Query(
            value = "SELECT n FROM Notification n WHERE n.deleted = false " +
                    "AND n.user.id = :userId " +
                    "ORDER BY n.createdAt DESC",
            countQuery = "SELECT COUNT(n) FROM Notification n WHERE n.deleted = false " +
                    "AND n.user.id = :userId"
    )
    Page<Notification> findByUserId(@Param("userId") Long userId, Pageable pageable);

    long countByUserIdAndStatusAndDeletedFalse(Long userId, NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.deleted = false " +
           "AND n.user.id = :userId AND n.status = :status " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") NotificationStatus status
    );

    @Query(
            value = "SELECT n FROM Notification n WHERE n.deleted = false " +
                    "AND n.user.id = :userId AND n.status = :status " +
                    "ORDER BY n.createdAt DESC",
            countQuery = "SELECT COUNT(n) FROM Notification n WHERE n.deleted = false " +
                    "AND n.user.id = :userId AND n.status = :status"
    )
    Page<Notification> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") NotificationStatus status,
            Pageable pageable
    );

    @Query("SELECT n FROM Notification n WHERE n.deleted = false " +
           "AND n.user.id = :userId " +
           "AND (:status IS NULL OR n.status = :status) " +
           "AND (:type IS NULL OR n.type = :type) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> search(
            @Param("userId") Long userId,
            @Param("status") NotificationStatus status,
            @Param("type") NotificationType type
    );

    @Query(
            value = "SELECT n FROM Notification n WHERE n.deleted = false " +
                    "AND n.user.id = :userId " +
                    "AND (:status IS NULL OR n.status = :status) " +
                    "AND (:type IS NULL OR n.type = :type) " +
                    "ORDER BY n.createdAt DESC",
            countQuery = "SELECT COUNT(n) FROM Notification n WHERE n.deleted = false " +
                    "AND n.user.id = :userId " +
                    "AND (:status IS NULL OR n.status = :status) " +
                    "AND (:type IS NULL OR n.type = :type)"
    )
    Page<Notification> search(
            @Param("userId") Long userId,
            @Param("status") NotificationStatus status,
            @Param("type") NotificationType type,
            Pageable pageable
    );
}
