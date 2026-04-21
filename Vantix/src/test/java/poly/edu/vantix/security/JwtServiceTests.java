package poly.edu.vantix.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import poly.edu.vantix.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class JwtServiceTests {

    private static final String TEST_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void generatedTokenCanRestoreAuthenticationPrincipal() {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000L);

        User user = new User();
        user.setId(11L);
        user.setUsername("admin");

        String token = jwtService.generateAccessToken(user);
        Authentication authentication = jwtService.getAuthentication(token);

        JwtUserPrincipal principal = assertInstanceOf(
                JwtUserPrincipal.class,
                authentication.getPrincipal()
        );

        assertEquals(11L, principal.getId());
        assertEquals("admin", principal.getUsername());
    }
}
