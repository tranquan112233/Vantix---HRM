package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix_hrm.entity.LeaveRequest;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    // Tìm toàn bộ đơn xin nghỉ của 1 nhân viên (dành cho User)
    List<LeaveRequest> findByEmployee_EmployeeIdOrderByCreatedAtDesc(Integer employeeId);

    // Tìm toàn bộ đơn theo trạng thái (dành cho HR/Admin duyệt)
    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(poly.edu.vantix_hrm.entity.LeaveStatus status);
}