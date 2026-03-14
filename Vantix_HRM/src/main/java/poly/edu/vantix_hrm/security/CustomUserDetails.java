package poly.edu.vantix_hrm.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import poly.edu.vantix_hrm.entity.User;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 🔥 ĐƠN QUYỀN: Lấy 1 Role duy nhất và bọc nó vào danh sách SimpleGrantedAuthority
        return Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().getRoleName())
        );
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Dùng email làm định danh đăng nhập
    }

    public String getDisplayUsername() {
        return user.getUsername();
    }

    // 🔥 ĐƠN QUYỀN: Trả về 1 String chứa Tên Role thay vì 1 Mảng (List)
    public String getRoleName() {
        return user.getRole().getRoleName();
    }
}