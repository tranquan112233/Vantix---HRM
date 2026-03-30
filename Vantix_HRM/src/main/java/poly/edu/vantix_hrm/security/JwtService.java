package poly.edu.vantix_hrm.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/*
 * JwtService
 * -------------------------------------------
 * Xử lý JWT: tạo token, đọc userId, kiểm tra hợp lệ
 *
 * Cách dùng:
 *   String token  = jwtService.generateToken(user.getId());
 *   Long   userId = jwtService.extractUserId(token);
 *   boolean valid = jwtService.validateToken(token);
 */
@Service
public class JwtService {

    // Đọc secret từ application.properties: jwt.secret=...
    @Value("${jwt.secret}")
    private String secret;

    // Đọc thời gian hết hạn từ application.properties: jwt.expiration=86400000
    @Value("${jwt.expiration}")
    private long expiration;

    // Lấy SecretKey từ secret string
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Tạo JWT token chứa userId
    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy userId từ token
    public Long extractUserId(String token) {
        return Long.parseLong(parse(token).getBody().getSubject());
    }

    // Kiểm tra token có hợp lệ không
    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Parse token → Claims
    private Jws<Claims> parse(String token) {
        return Jwts.parser()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);
    }
}