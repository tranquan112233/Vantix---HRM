package poly.edu.vantix_hrm.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import poly.edu.vantix_hrm.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Lặp qua danh sách các quyền (Set<Role>) của User
        // và biến nó thành danh sách SimpleGrantedAuthority cho Spring Security
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());
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

    // Đã đổi từ getRole() -> getRoles() trả về List<String> thay vì 1 String
    public List<String> getRoles() {
        return user.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toList());
    }
}