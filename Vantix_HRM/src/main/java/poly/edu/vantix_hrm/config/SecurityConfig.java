package poly.edu.vantix_hrm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import poly.edu.vantix_hrm.security.CustomUserDetailsService;
import poly.edu.vantix_hrm.service.JwtService;

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

        http
                .csrf(csrf -> csrf.disable())

                // 🔥 BẬT CORS
                .cors(cors -> {})

                .authorizeHttpRequests(auth -> auth
                        // 🔥 AUTH
                        .requestMatchers("/api/auth/**").permitAll()

                        // 🔥 TASK
                        .requestMatchers("/api/tasks/**").permitAll()

                        // 🔥 FIX QUAN TRỌNG
                        .requestMatchers("/api/employees/**").permitAll()

                        // các API khác
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

}