package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.MakeupCheckoutRequest;
import poly.edu.vantix.entity.enums.MakeupCheckoutStatus;

import java.util.List;
import java.util.Optional;

public interface MakeupCheckoutRequestRepository extends JpaRepository<MakeupCheckoutRequest, Long> {

    @Query("SELECT r FROM MakeupCheckoutRequest r " +
           "JOIN FETCH r.employee e " +
           "LEFT JOIN FETCH e.department " +
           "JOIN FETCH r.attendance a " +
           "LEFT JOIN FETCH a.schedule s " +
           "LEFT JOIN FETCH s.shift " +
           "LEFT JOIN FETCH r.decidedBy " +
           "WHERE r.deleted = false " +
           "AND (:employeeId IS NULL OR r.employee.id = :employeeId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "ORDER BY r.createdAt DESC")
    List<MakeupCheckoutRequest> search(
            @Param("employeeId") Long employeeId,
            @Param("status") MakeupCheckoutStatus status
    );

    @Query("SELECT r FROM MakeupCheckoutRequest r " +
           "JOIN FETCH r.employee e " +
           "LEFT JOIN FETCH e.department " +
           "JOIN FETCH r.attendance a " +
           "LEFT JOIN FETCH a.schedule s " +
           "LEFT JOIN FETCH s.shift " +
           "LEFT JOIN FETCH r.decidedBy " +
           "WHERE r.deleted = false AND r.id = :id")
    Optional<MakeupCheckoutRequest> findActiveById(@Param("id") Long id);

    @Query("SELECT COUNT(r) FROM MakeupCheckoutRequest r " +
           "WHERE r.deleted = false " +
           "AND r.attendance.id = :attendanceId " +
           "AND r.status = poly.edu.vantix.entity.enums.MakeupCheckoutStatus.PENDING " +
           "AND (:excludeId IS NULL OR r.id <> :excludeId)")
    long countPendingForAttendance(
            @Param("attendanceId") Long attendanceId,
            @Param("excludeId") Long excludeId
    );
}
