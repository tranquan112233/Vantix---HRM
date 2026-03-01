package poly.edu.vantix_hrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.DTO.ProfileDTO;
import poly.edu.vantix_hrm.DTO.ProfileUpdateDTO;
import poly.edu.vantix_hrm.entity.Employee;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final EmployeeRepository employeeRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public ProfileDTO getMyProfile() {

        User user = getCurrentUser();

        Employee emp = employeeRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        ProfileDTO dto = new ProfileDTO();

        dto.setUserId(user.getId()); // ⭐ QUAN TRỌNG
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        dto.setFullName(emp.getFullName());
        dto.setPhone(emp.getPhone());
        dto.setAddress(emp.getAddress());
        dto.setBirthDate(emp.getBirthDate());
        dto.setGender(emp.getGender().name());

        dto.setDepartment(
                emp.getDepartment() != null ?
                        emp.getDepartment().getName() : null);

        dto.setPosition(
                emp.getPosition() != null ?
                        emp.getPosition().getName() : null);

        return dto;
    }

    public void updateProfile(ProfileUpdateDTO req) {

        User user = getCurrentUser();

        Employee emp = employeeRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        emp.setFullName(req.getFullName());
        emp.setPhone(req.getPhone());
        emp.setAddress(req.getAddress());
        emp.setBirthDate(req.getBirthDate());

        if (req.getGender() != null) {
            emp.setGender(Employee.Gender.valueOf(req.getGender()));
        }

        employeeRepository.save(emp);
    }
}