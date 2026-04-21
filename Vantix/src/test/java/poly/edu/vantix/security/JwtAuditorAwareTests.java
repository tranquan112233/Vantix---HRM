package poly.edu.vantix.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuditorAwareTests {

    private final JwtAuditorAware auditorAware = new JwtAuditorAware();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsUserIdFromJwtPrincipal() {
        JwtUserPrincipal principal = new JwtUserPrincipal(
                7L,
                "admin",
                List.of(new SimpleGrantedAuthority("USER_VIEW"))
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                )
        );

        assertEquals(7L, auditorAware.getCurrentAuditor().orElseThrow());
    }

    @Test
    void returnsEmptyWhenNotAuthenticated() {
        assertTrue(auditorAware.getCurrentAuditor().isEmpty());
    }
}
