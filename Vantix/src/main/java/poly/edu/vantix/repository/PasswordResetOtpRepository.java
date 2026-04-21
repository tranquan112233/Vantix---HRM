package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix.entity.PasswordResetOtp;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findByRequestIdAndDeletedFalse(String requestId);

    Optional<PasswordResetOtp> findByResetTokenAndDeletedFalse(String resetToken);
}
