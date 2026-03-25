package poly.edu.vantix_hrm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod; // Thêm dòng này
import org.springframework.security.config.Customizer; // Thêm dòng này
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // Thêm dòng này
import org.springframework.web.cors.CorsConfigurationSource; // Thêm dòng này
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Thêm dòng này
import poly.edu.vantix_hrm.security.CustomUserDetailsService;
import poly.edu.vantix_hrm.service.JwtService;

import java.util.Arrays; // Thêm dòng này

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 🔥 1. BẬT CORS Ở ĐÂY
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 🔥 2. Cho phép các request thăm dò OPTIONS đi qua mượt mà không bị bắt thẻ Token
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(
                                jwtService,
                                userDetailsService
                        ),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    // 🔥 3. CỤC NÀY ĐỂ KHAI BÁO CHO PHÉP VUE (5173) TRUY CẬP
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Link Frontend Vue của bác (không có dấu gạch chéo ở cuối nhé)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

        // Cấp phép cho mọi method, đặc biệt là OPTIONS
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Cấp phép gửi Token qua Header
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));

        // Cấp phép gửi thông tin xác thực (Credentials)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}