package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.leave.*;
import poly.edu.vantix_hrm.entity.LeaveStatus;
import poly.edu.vantix_hrm.service.LeaveRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Tạm thời mở CORS để Vue.js gọi được, sau này cấu hình lại cho an toàn
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    // API: Nộp đơn xin nghỉ
    @PreAuthorize("hasAuthority('LEAVE_CREATE')")
    @PostMapping
    public ResponseEntity<LeaveResponseDTO> createLeaveRequest(@RequestBody LeaveRequestDTO dto) {
        LeaveResponseDTO createdRequest = leaveRequestService.createLeaveRequest(dto);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    // API: Xem lịch sử đơn của nhân viên đang đăng nhập
    @PreAuthorize("hasAuthority('LEAVE_VIEW')")
    @GetMapping("/my-requests")
    public ResponseEntity<List<LeaveResponseDTO>> getMyRequests() {
        return ResponseEntity.ok(leaveRequestService.getMyLeaveRequests());
    }

    // API: Xem danh sách đơn cần duyệt (Dành cho HR/Admin)
    @PreAuthorize("hasAuthority('LEAVE_MANAGE')")
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveResponseDTO>> getPendingRequests() {
        List<LeaveResponseDTO> requests = leaveRequestService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    // API: HR/Admin duyệt hoặc từ chối đơn
    // Chuyển status qua param, ví dụ: PUT /api/leaves/1/status?status=APPROVED&approverId=2
    @PreAuthorize("hasAuthority('LEAVE_MANAGE')")
    @PutMapping("/{leaveId}/status")
    public ResponseEntity<LeaveResponseDTO> updateStatus(
            @PathVariable Long leaveId,
            @RequestParam LeaveStatus status,
            @RequestParam(required = false) String rejectionReason) {
        LeaveResponseDTO updatedRequest = leaveRequestService.updateLeaveStatus(leaveId, status, rejectionReason);
        return ResponseEntity.ok(updatedRequest);
    }
}