package poly.edu.vantix_hrm.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.UserRepository;

import java.io.IOException;
import java.util.List;

/*
 * JwtFilter
 * -------------------------------------------
 * Chạy mỗi request, kiểm tra JWT trong header Authorization
 *
 * Luồng xử lý:
 *   1. Đọc header Authorization: Bearer <token>
 *   2. Validate token
 *   3. Lấy userId từ token → tìm user trong DB
 *   4. Set Authentication vào SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Không có token → bỏ qua, tiếp tục filter chain
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // Token không hợp lệ → bỏ qua
        if (!jwtService.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Chưa có authentication trong context → set vào
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Long userId = jwtService.extractUserId(token);
            // Sử dụng phương thức với EntityGraph để fetch role và permissions
            User user = userRepository.findByIdWithRoleAndPermissions(userId).orElse(null);

            if (user != null && Boolean.FALSE.equals(user.getDeleted()) && user.getStatus() != User.UserStatus.LOCKED) {
                // Map permissions → authorities
                List<GrantedAuthority> authorities = user.getRole() != null
                        ? user.getRole().getPermissions().stream()
                                .<GrantedAuthority>map(permission -> new SimpleGrantedAuthority(permission.getName()))
                                .toList()
                        : List.of();

                CustomUserPrincipal principal = new CustomUserPrincipal(
                        user.getId(),
                        user.getUsername(),
                        user.getPassword(),
                        authorities
                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, authorities
                        );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }



}