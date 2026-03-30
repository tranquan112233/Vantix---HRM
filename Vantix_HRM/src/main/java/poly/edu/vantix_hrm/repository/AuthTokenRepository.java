package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import poly.edu.vantix_hrm.entity.AuthToken;
import poly.edu.vantix_hrm.entity.User;

import java.util.Optional;

/*
 * AuthTokenRepository
 * -------------------------------------------
 * Truy vấn DB cho entity AuthToken
 */
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findTopByTokenAndTypeAndUsedFalseOrderByCreatedAtDesc(
            String token,
            AuthToken.TokenType type
    );

    // Tìm token OTP mới nhất của user theo loại (chưa dùng, còn hạn)
    Optional<AuthToken> findTopByUserAndTypeAndUsedFalseOrderByCreatedAtDesc(
            User user, AuthToken.TokenType type);

    // Xóa tất cả token cũ của user theo loại (dọn dẹp trước khi tạo mới)
    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.user = :user AND t.type = :type")
    void deleteAllByUserAndType(User user, AuthToken.TokenType type);
}