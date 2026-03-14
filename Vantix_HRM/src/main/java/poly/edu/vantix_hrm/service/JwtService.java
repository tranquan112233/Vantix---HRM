package poly.edu.vantix_hrm.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import poly.edu.vantix_hrm.entity.User;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
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
    public String generateToken(User user) {
    // Lấy tên của 1 Role duy nhất bọc vào List (để Frontend vẫn đọc dạng mảng cho đỡ phải sửa code Vue)
        List<String> roles = java.util.List.of(user.getRole().getRoleName());

        // 🔥 THÊM ĐOẠN NÀY: Lấy chuỗi permissions của User cắt thành Mảng
        List<String> userPermissions = new ArrayList<>();
        if (user.getPermissions() != null && !user.getPermissions().trim().isEmpty()) {
            userPermissions = Arrays.asList(user.getPermissions().split(","));
        }

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("username", user.getUsername())
                .claim("roles", roles)
                .claim("permissions", userPermissions)

                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
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