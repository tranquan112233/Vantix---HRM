package poly.edu.vantix.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import poly.edu.vantix.entity.Permission;
import poly.edu.vantix.entity.User;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JwtService {

    public static final String USER_ID_CLAIM = "userId";
    public static final String PERMISSIONS_CLAIM = "permissions";

    private final SecretKey signingKey;
    private final long accessTokenExpiration;

    public JwtService(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration
    ) {
        if (accessTokenExpiration <= 0) {
            throw new IllegalArgumentException("accessTokenExpiration must be greater than 0");
        }

        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public String generateAccessToken(User user) {
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(user.getId(), "user id must not be null");
        Objects.requireNonNull(user.getUsername(), "username must not be null");

        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(USER_ID_CLAIM, user.getId())
                .claim(PERMISSIONS_CLAIM, extractPermissionNames(user))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseAccessToken(token);
        Long userId = getRequiredUserId(claims);
        String username = claims.getSubject();

        if (username == null || username.isBlank()) {
            throw new JwtException("JWT subject is required");
        }

        List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
        JwtUserPrincipal principal = new JwtUserPrincipal(userId, username, authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    private Long getRequiredUserId(Claims claims) {
        Object userId = claims.get(USER_ID_CLAIM);

        if (userId instanceof Number number) {
            return number.longValue();
        }

        if (userId instanceof String text && !text.isBlank()) {
            return Long.valueOf(text);
        }

        throw new JwtException("JWT userId claim is required");
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object permissions = claims.get(PERMISSIONS_CLAIM);

        if (!(permissions instanceof Collection<?> values)) {
            return List.of();
        }

        return values.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(permission -> !permission.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    private List<String> extractPermissionNames(User user) {
        if (user.getRole() == null || user.getRole().getPermissions() == null) {
            return List.of();
        }

        return user.getRole().getPermissions().stream()
                .map(Permission::getName)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
    }
}
