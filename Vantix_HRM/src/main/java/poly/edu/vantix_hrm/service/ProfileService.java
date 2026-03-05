package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import poly.edu.vantix_hrm.DTO.UserProfileDTO;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.EmployeeRepository;
import poly.edu.vantix_hrm.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository; // Bổ sung UserRepository để tìm User từ Token

    private final String UPLOAD_DIR = "uploads/avatars/";
    private final String BASE_URL = "http://localhost:8080/avatars/";

    // =================================================================================
    // 1. TÍNH NĂNG MỚI: Lấy Profile dựa vào username (Trích xuất từ JWT Token)
    // =================================================================================
    public UserProfileDTO getMyProfile(String username) {
        // 1. Tìm User dựa vào username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        // 2. Tìm Employee dựa vào user_id
        Employee emp = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Tài khoản này chưa có hồ sơ nhân viên"));

        // 3. Gọi lại hàm getProfile bên dưới để map dữ liệu
        return getProfile(emp.getEmployeeId());
    }

    // =================================================================================
    // 2. Lấy toàn bộ thông tin Profile (Dùng nội bộ hoặc cho Admin xem)
    // =================================================================================
    public UserProfileDTO getProfile(Integer employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setEmployeeId(emp.getEmployeeId());
        dto.setFullName(emp.getFullName());
        dto.setPhone(emp.getPhone());
        dto.setAddress(emp.getAddress());
        dto.setBirthDate(emp.getBirthDate());

        if (emp.getGender() != null) {
            dto.setGender(emp.getGender().name()); // Chuyển Enum thành String
        }

        // Lấy thông tin từ bảng Users liên kết
        if (emp.getUser() != null) {
            dto.setUsername(emp.getUser().getUsername());
            dto.setEmail(emp.getUser().getEmail());
        }

        // Lấy thông tin từ bảng Departments liên kết
        if (emp.getDepartment() != null) {
            dto.setDepartment(emp.getDepartment().getName()); // Lấy tên phòng ban
        }

        // Lấy thông tin từ bảng Positions liên kết
        if (emp.getPosition() != null) {
            dto.setPosition(emp.getPosition().getName()); // Lấy tên chức vụ
        }

        // Kiểm tra ảnh vật lý
        Path avatarPath = Paths.get(UPLOAD_DIR + "avatar_" + employeeId + ".jpg");
        if (Files.exists(avatarPath)) {
            dto.setAvatarUrl(BASE_URL + "avatar_" + employeeId + ".jpg");
        } else {
            dto.setAvatarUrl(null);
        }

        return dto;
    }

    // =================================================================================
    // 3. Cập nhật thông tin Profile (không cho đổi email/username)
    // =================================================================================
    @Transactional
    public UserProfileDTO updateProfile(Integer employeeId, UserProfileDTO dto) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // Cập nhật các trường được phép thay đổi từ UI
        emp.setFullName(dto.getFullName());
        emp.setPhone(dto.getPhone());
        emp.setAddress(dto.getAddress());
        emp.setBirthDate(dto.getBirthDate());

        // Ánh xạ lại string gender từ Frontend về Enum trong Database (nếu bạn dùng Enum)
        if (dto.getGender() != null && !dto.getGender().isEmpty()) {
            // Giả sử class Employee có khai báo enum Gender { MALE, FEMALE, OTHER }
            // emp.setGender(Employee.Gender.valueOf(dto.getGender()));
        }

        employeeRepository.save(emp);
        return getProfile(employeeId); // Trả về data mới nhất sau khi lưu
    }

    // =================================================================================
    // 4. Xử lý upload Avatar (Lưu file vật lý)
    // =================================================================================
    public String uploadAvatar(Integer employeeId, MultipartFile file) {
        try {
            // Tạo thư mục nếu chưa có
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Ép tên file thành dạng: avatar_1.jpg (1 là employeeId)
            String fileName = "avatar_" + employeeId + ".jpg";
            Path filePath = uploadPath.resolve(fileName);

            // Lưu file (Nếu đã có ảnh cũ thì sẽ tự động ghi đè lên - REPLACE_EXISTING)
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn để Frontend hiển thị luôn
            return BASE_URL + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu ảnh: " + e.getMessage());
        }
    }
}