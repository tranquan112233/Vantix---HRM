package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.edu.vantix_hrm.entity.LeaveRequest;
import poly.edu.vantix_hrm.entity.LeaveStatus;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    // Tìm toàn bộ đơn xin nghỉ của 1 nhân viên (dành cho User)
    List<LeaveRequest> findByEmployee_IdOrderByCreatedAtDesc(Long employeeId);

    // Tìm toàn bộ đơn theo trạng thái (dành cho HR/Admin duyệt)
    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveStatus status);

    // Kiểm tra trùng ngày nghỉ (loại trừ đơn đã bị từ chối)
    @Query("SELECT COUNT(lr) > 0 FROM LeaveRequest lr " +
           "WHERE lr.employee.id = :employeeId " +
           "AND lr.status != 'REJECTED' " +
           "AND lr.startDate <= :endDate " +
           "AND lr.endDate >= :startDate")
    Boolean existsOverlappingLeave(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}