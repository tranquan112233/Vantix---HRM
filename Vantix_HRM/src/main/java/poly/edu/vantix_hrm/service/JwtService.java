package poly.edu.vantix_hrm.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.entity.User;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final String SECRET =
            "mysupersecretkeymysupersecretkeymysupersecretkeymysupersecretkey";

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Tạo token
    // Tạo token
    public String generateToken(User user) {

        // Chuyển Set<Role> thành List<String>
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toList());

        // CHỖ NÀY MỚI THÊM: Xử lý mảng menu bị chặn
        List<String> disabledMenus = new java.util.ArrayList<>();
        if (user.getDisabledMenus() != null && !user.getDisabledMenus().trim().isEmpty()) {
            disabledMenus = java.util.Arrays.asList(user.getDisabledMenus().split(","));
        }

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("username", user.getUsername())
                .claim("roles", roles)
                .claim("disabledMenus", disabledMenus) // Ném mảng menu bị chặn vào Token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getKey())
                .compact();
    }

    // Lấy email từ token
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Kiểm tra token hợp lệ
    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}