package poly.edu.vantix_hrm.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import poly.edu.vantix_hrm.security.CustomUserPrincipal;

import java.util.Optional;

/*
 * AuditorAwareImpl
 * -------------------------------------------
 * Tự động ghi createdBy / updatedBy khi lưu Entity
 *
 * Trả về userId của user đang đăng nhập
 * Nếu chưa đăng nhập (anonymous) → trả về 0L
 *
 * Dùng kết hợp với @CreatedBy / @LastModifiedBy trong Entity:
 *   @CreatedBy
 *   private Long createdBy;
 */
@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Chưa đăng nhập hoặc anonymous → trả về 0
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.of(0L);
        }

        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        return Optional.of(principal.getUserId());
    }
}