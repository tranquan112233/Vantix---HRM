package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix.entity.LeaveRequest;
import poly.edu.vantix.entity.enums.LeaveRequestStatus;
import poly.edu.vantix.entity.enums.LeaveType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("SELECT DISTINCT lr FROM LeaveRequest lr " +
           "JOIN FETCH lr.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH lr.handoverEmployee h " +
           "LEFT JOIN FETCH h.department " +
           "LEFT JOIN FETCH lr.decidedBy " +
           "WHERE lr.deleted = false " +
           "AND (:employeeId IS NULL OR e.id = :employeeId) " +
           "AND (:keyword IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(lr.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(lr.emergencyContact) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR lr.status = :status) " +
           "AND (:type IS NULL OR lr.type = :type) " +
           "AND (:rangeStart IS NULL OR lr.startDate <= :rangeEnd) " +
           "AND (:rangeEnd IS NULL OR lr.endDate >= :rangeStart) " +
           "ORDER BY lr.id DESC")
    List<LeaveRequest> search(
            @Param("employeeId") Long employeeId,
            @Param("keyword") String keyword,
            @Param("status") LeaveRequestStatus status,
            @Param("type") LeaveType type,
            @Param("rangeStart") LocalDate rangeStart,
            @Param("rangeEnd") LocalDate rangeEnd
    );

    @Query("SELECT DISTINCT lr FROM LeaveRequest lr " +
           "JOIN FETCH lr.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH lr.handoverEmployee h " +
           "LEFT JOIN FETCH h.department " +
           "LEFT JOIN FETCH lr.decidedBy " +
           "WHERE lr.id = :id AND lr.deleted = false")
    Optional<LeaveRequest> findActiveById(@Param("id") Long id);

    @Query("SELECT COUNT(lr) > 0 FROM LeaveRequest lr " +
           "WHERE lr.deleted = false " +
           "AND lr.employee.id = :employeeId " +
           "AND lr.status IN :statuses " +
           "AND (:excludeId IS NULL OR lr.id <> :excludeId) " +
           "AND lr.startDate <= :endDate " +
           "AND lr.endDate >= :startDate")
    boolean existsOverlappingRequest(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<LeaveRequestStatus> statuses,
            @Param("excludeId") Long excludeId
    );
}
