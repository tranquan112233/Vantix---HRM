package poly.edu.vantix_hrm.security;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/*
 * CustomUserPrincipal
 * -------------------------------------------
 * Lưu thông tin user đang đăng nhập vào SecurityContext
 *
 * Cách lấy user đang đăng nhập trong Controller/Service:
 *   CustomUserPrincipal principal =
 *       (CustomUserPrincipal) SecurityContextHolder.getContext()
 *           .getAuthentication().getPrincipal();
 *   Long userId = principal.getUserId();
 */
@Getter
@AllArgsConstructor
public class CustomUserPrincipal implements UserDetails {

    // ID của user đang đăng nhập
    private Long userId;

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return true; }
}