package poly.edu.vantix.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.request.ChangePasswordRequest;
import poly.edu.vantix.dto.request.UpdateProfileEmailRequest;
import poly.edu.vantix.dto.response.EmployeeResponse;
import poly.edu.vantix.dto.response.ProfileResponse;
import poly.edu.vantix.dto.response.UserResponse;
import poly.edu.vantix.entity.Employee;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.UserStatus;
import poly.edu.vantix.exception.BusinessException;
import poly.edu.vantix.exception.UnauthorizedException;
import poly.edu.vantix.repository.EmployeeRepository;
import poly.edu.vantix.repository.UserRepository;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        return buildProfile(findCurrentUser(userId));
    }

    @Transactional
    public ProfileResponse updateEmail(Long userId, UpdateProfileEmailRequest request) {
        User user = findCurrentUser(userId);
        String email = request.getEmail().trim();

        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new BusinessException("email", "Email already exists");
                });

        user.setEmail(email);
        return buildProfile(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findCurrentUser(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("currentPassword", "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findCurrentUser(Long userId) {
        User user = userRepository.findActiveWithRoleAndPermissionsById(userId)
                .orElseThrow(() -> new UnauthorizedException("User is no longer available"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is locked");
        }

        return user;
    }

    private ProfileResponse buildProfile(User user) {
        Employee employee = employeeRepository.findActiveByUserId(user.getId()).orElse(null);

        return ProfileResponse.builder()
                .account(UserResponse.fromEntity(user))
                .employee(employee == null ? null : EmployeeResponse.fromEntity(employee))
                .build();
    }
}
