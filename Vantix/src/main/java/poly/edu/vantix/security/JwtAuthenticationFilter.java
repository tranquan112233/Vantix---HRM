package poly.edu.vantix.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import poly.edu.vantix.entity.Permission;
import poly.edu.vantix.entity.User;
import poly.edu.vantix.entity.enums.UserStatus;
import poly.edu.vantix.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Authentication authentication = refreshAuthentication(jwtService.getAuthentication(token), token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Authentication refreshAuthentication(Authentication tokenAuthentication, String token) {
        if (!(tokenAuthentication.getPrincipal() instanceof JwtUserPrincipal tokenPrincipal)) {
            throw new JwtException("JWT principal is invalid");
        }

        User user = userRepository.findActiveWithRoleAndPermissionsById(tokenPrincipal.getId())
                .orElseThrow(() -> new JwtException("JWT user is no longer available"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new JwtException("JWT user is locked");
        }

        List<SimpleGrantedAuthority> authorities = user.getRole() == null
                || Boolean.TRUE.equals(user.getRole().getDeleted())
                || user.getRole().getPermissions() == null
                ? List.of()
                : user.getRole().getPermissions().stream()
                        .map(Permission::getName)
                        .filter(Objects::nonNull)
                        .filter(permission -> !permission.isBlank())
                        .sorted()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        JwtUserPrincipal principal = new JwtUserPrincipal(
                user.getId(),
                user.getUsername(),
                authorities
        );

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX.length()).trim();
            return token.isEmpty() ? null : token;
        }

        if (!request.getRequestURI().endsWith("/ws/notifications")) {
            return null;
        }

        String token = request.getParameter("access_token");
        if (token == null) {
            return null;
        }

        return token.isEmpty() ? null : token;
    }
}
