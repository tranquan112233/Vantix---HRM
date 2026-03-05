package poly.edu.vantix_hrm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.DTO.ProfileDTO;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.repository.EmployeeRepository;

import java.io.File;

@Service
public class ProfileService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public ProfileDTO getProfileByUsername(String username) {
        Employee employee = employeeRepository.findByUser(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ nhân viên cho tài khoản này!"));

        ProfileDTO dto = new ProfileDTO();
        dto.setEmployeeId(employee.getEmployeeId());

        if (employee.getUser() != null) {
            dto.setUsername(employee.getUser().getUsername());
            dto.setEmail(employee.getUser().getEmail());
        }

        dto.setFullName(employee.getFullName());

        if (employee.getGender() != null) {
            dto.setGender(employee.getGender().name());
        }

        dto.setBirthDate(employee.getBirthDate());
        dto.setPhone(employee.getPhone());
        dto.setAddress(employee.getAddress());

        if (employee.getWorkStatus() != null) {
            dto.setWorkStatus(employee.getWorkStatus().name());
        }

        if (employee.getDepartment() != null) {
            dto.setDepartmentName(employee.getDepartment().getName());
        }
        if (employee.getPosition() != null) {
            dto.setPositionName(employee.getPosition().getName());
        }

        // TÌM ẢNH ĐẠI DIỆN DỰA TRÊN USERNAME
        String avatarUrl = "/uploads/avatars/1.png"; // Cần chuẩn bị sẵn 1 ảnh mặc định trong thư mục này
        File folder = new File("uploads/avatars");
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.startsWith(username + "."));
            if (files != null && files.length > 0) {
                // Nếu tìm thấy file bắt đầu bằng "username.", lấy tên file đó
                avatarUrl = "/uploads/avatars/" + files[0].getName();
            }
        }
        dto.setAvatarUrl(avatarUrl);

        return dto;
    }
}