package poly.edu.vantix_hrm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.vantix_hrm.dto.leave.*;
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
    @PostMapping
    public ResponseEntity<LeaveResponseDTO> createLeaveRequest(@RequestBody LeaveRequestDTO dto) {
        LeaveResponseDTO createdRequest = leaveRequestService.createLeaveRequest(dto);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    // API: Xem lịch sử đơn của nhân viên đang đăng nhập
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveResponseDTO>> getMyLeaveRequests(@PathVariable Integer employeeId) {
        List<LeaveResponseDTO> requests = leaveRequestService.getLeaveRequestsByEmployee(employeeId);
        return ResponseEntity.ok(requests);
    }

    // API: Xem danh sách đơn cần duyệt (Dành cho HR/Admin)
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveResponseDTO>> getPendingRequests() {
        List<LeaveResponseDTO> requests = leaveRequestService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    // API: HR/Admin duyệt hoặc từ chối đơn
    // Chuyển status qua param, ví dụ: PUT /api/leaves/1/status?status=APPROVED&approverId=2
    @PutMapping("/{leaveId}/status")
    public ResponseEntity<LeaveResponseDTO> updateStatus(
            @PathVariable Integer leaveId,
            @RequestParam LeaveStatus status,
            @RequestParam Integer approverId) {
        LeaveResponseDTO updatedRequest = leaveRequestService.updateLeaveStatus(leaveId, status, approverId);
        return ResponseEntity.ok(updatedRequest);
    }
}