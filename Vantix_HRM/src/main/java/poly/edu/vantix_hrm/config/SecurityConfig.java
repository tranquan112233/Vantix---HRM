package poly.edu.vantix_hrm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import poly.edu.vantix_hrm.security.JwtAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())   // ❌ tắt csrf
                .cors(cors -> {})               // ✅ bật cors
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**",
                                         "/avatars/**").permitAll()
                        .requestMatchers("/api/profile/avatar").authenticated()
                        .requestMatchers("/api/leave-types/**").permitAll() // ✅ thêm dòng này
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable()); // ❌ tắt form login mặc định

        http.addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
