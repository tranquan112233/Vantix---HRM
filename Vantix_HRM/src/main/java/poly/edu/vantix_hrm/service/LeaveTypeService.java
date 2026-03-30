package poly.edu.vantix_hrm.service;

import poly.edu.vantix_hrm.entity.LeaveType;
import poly.edu.vantix_hrm.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LeaveTypeService {

    @Autowired
    private LeaveTypeRepository repository;

    public List<LeaveType> getAllLeaveTypes() {
        return repository.findAll();
    }

    public LeaveType createLeaveType(LeaveType leaveType) {
        if (repository.existsByTypeName(leaveType.getTypeName())) {
            throw new RuntimeException("Tên loại nghỉ phép này đã tồn tại!");
        }
        return repository.save(leaveType);
    }

    // Trong file LeaveTypeService.java (Chỗ hàm update)
    public LeaveType updateLeaveType(Integer id, LeaveType updatedData) {
        LeaveType existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ID: " + id));

        existing.setTypeName(updatedData.getTypeName());

        // 🔥 Đổi thành getIsPaid() và setIsPaid()
        existing.setIsPaid(updatedData.getIsPaid());

        return repository.save(existing);
    }

    public void deleteLeaveType(Integer id) {
        repository.deleteById(id);
    }
}