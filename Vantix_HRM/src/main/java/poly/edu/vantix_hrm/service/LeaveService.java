package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.DTO.LeaveRequestDTO;
import poly.edu.vantix_hrm.entity.*;
import poly.edu.vantix_hrm.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRepo;
    private final LeaveTypeRepository leaveTypeRepo;
    private final EmployeeRepository employeeRepo;

    // Tạo đơn nghỉ
    public LeaveRequest create(LeaveRequestDTO dto) {

        Employee emp = employeeRepo.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveType type = leaveTypeRepo.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(emp);
        leave.setLeaveType(type);
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setTotalShift(dto.getTotalShift());
        leave.setReason(dto.getReason());

        return leaveRepo.save(leave);
    }

    // Duyệt đơn
    public LeaveRequest approve(Integer leaveId, Integer approverId) {

        LeaveRequest leave = leaveRepo.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        Employee approver = employeeRepo.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        leave.setStatus(LeaveRequest.Status.APPROVED);
        leave.setApprovedBy(approver);

        return leaveRepo.save(leave);
    }

    // Từ chối
    public LeaveRequest reject(Integer leaveId, Integer approverId) {

        LeaveRequest leave = leaveRepo.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        Employee approver = employeeRepo.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        leave.setStatus(LeaveRequest.Status.REJECTED);
        leave.setApprovedBy(approver);

        return leaveRepo.save(leave);
    }

    public List<LeaveRequest> getAll() {
        return leaveRepo.findAll();
    }

    public List<LeaveRequest> getByEmployee(Integer employeeId) {
        return leaveRepo.findByEmployeeEmployeeId(employeeId);
    }
}