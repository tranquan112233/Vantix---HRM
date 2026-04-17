package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix_hrm.dto.leave.*;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.LeaveRequest;
import poly.edu.vantix_hrm.entity.LeaveStatus;
import poly.edu.vantix_hrm.entity.LeaveType;
import poly.edu.vantix_hrm.repository.EmployeeRepository;
import poly.edu.vantix_hrm.repository.LeaveRequestRepository;
import poly.edu.vantix_hrm.repository.LeaveTypeRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    // 1. Nộp đơn xin nghỉ mới
    public LeaveResponseDTO createLeaveRequest(LeaveRequestDTO dto) {
        // 1. Lấy Email của người đang đăng nhập từ Spring Security
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        // 2. Tìm Employee dựa trên Email (thông qua bảng User)
        Employee employee = employeeRepository.findByUser_Username(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên ứng với tài khoản này"));
        // 3. Tìm loại nghỉ phép
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại nghỉ phép"));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (dto.getStartDate().isBefore(today)) {
            throw new RuntimeException("Ngày bắt đầu không được trong quá khứ");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu");
        }

        Boolean hasOverlap = leaveRequestRepository.existsOverlappingLeave(
                employee.getId(), dto.getStartDate(), dto.getEndDate());
        if (Boolean.TRUE.equals(hasOverlap)) {
            throw new RuntimeException("Bạn đã có đơn xin nghỉ trong khoảng thời gian này");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setTotalShift(dto.getTotalShift());
        leaveRequest.setReason(dto.getReason());
        leaveRequest.setStatus(LeaveStatus.PENDING);

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
// CHÈN THÊM: Báo cho Admin (Giả sử ID Admin/Manager là 1 hoặc lấy từ phòng ban)
        notificationService.sendNotification(1L, "📩 Đơn nghỉ phép mới",
                employee.getFullName() + " vừa gửi đơn xin nghỉ.", "LEAVE_MANAGE", "/leaves-manager");
        return mapToResponseDTO(savedRequest);
    }

    // 2. Lấy danh sách đơn của 1 nhân viên
    public List<LeaveResponseDTO> getMyLeaveRequests() {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

// Đổi findByUser_Email thành findByUser_Username
        Employee employee = employeeRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản này chưa có hồ sơ Nhân viên!"));

        // 3. Lấy danh sách đơn dựa trên ID vừa tìm được
        List<LeaveRequest> requests = leaveRequestRepository
                .findByEmployee_IdOrderByCreatedAtDesc(employee.getId());

        return requests.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    // 3. Lấy danh sách đơn đang chờ duyệt (Cho HR/Admin)
    public List<LeaveResponseDTO> getPendingRequests() {
        List<LeaveRequest> requests = leaveRequestRepository.findByStatusOrderByCreatedAtDesc(LeaveStatus.PENDING);
        return requests.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    // 4. Duyệt hoặc Từ chối đơn
    @Transactional
    public LeaveResponseDTO updateLeaveStatus(Long leaveId, LeaveStatus status, String rejectionReason) {

        // 1. Tìm cái đơn xin nghỉ cần duyệt
        LeaveRequest request = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn xin nghỉ"));

        // 2. Lấy Email của HR/Admin đang đăng nhập từ Token
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Employee approver = employeeRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản HR/Admin này chưa có hồ sơ Nhân viên!"));

        // 3. Cập nhật trạng thái và người duyệt
        request.setStatus(status);
        request.setApprovedBy(approver);

        if (status == LeaveStatus.REJECTED && rejectionReason != null && !rejectionReason.isBlank()) {
            request.setRejectionReason(rejectionReason);
        }

        // 2. Trong hàm updateLeaveStatus (Admin duyệt đơn)
        LeaveRequest updatedRequest = leaveRequestRepository.save(request);
        // CHÈN THÊM: Báo cho nhân viên kết quả
        String title = (status == LeaveStatus.APPROVED) ? "✅ Đơn nghỉ phép đã duyệt" : "❌ Đơn nghỉ phép bị từ chối";
        notificationService.sendNotification(request.getEmployee().getUser().getId(), title,
                "Đơn nghỉ ngày " + request.getStartDate() + " của bạn đã được xử lý.", "LEAVE", "/my-notifications");
        return mapToResponseDTO(updatedRequest);
    }

    // Hàm phụ trợ: Chuyển Entity sang DTO
    private LeaveResponseDTO mapToResponseDTO(LeaveRequest entity) {
        LeaveResponseDTO dto = new LeaveResponseDTO();
        dto.setLeaveId(entity.getLeaveId());
        dto.setEmployeeName(entity.getEmployee().getFullName());
        dto.setLeaveTypeName(entity.getLeaveType().getTypeName());
        dto.setIsPaid(entity.getLeaveType().getIsPaid());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setTotalShift(entity.getTotalShift());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());

        dto.setRejectionReason(entity.getRejectionReason());

        if (entity.getApprovedBy() != null) {
            dto.setApprovedByName(entity.getApprovedBy().getFullName());
        }
        return dto;
    }

}