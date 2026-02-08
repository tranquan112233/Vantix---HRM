package poly.edu.vantix_hrm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import poly.edu.vantix_hrm.entity.Role;
import poly.edu.vantix_hrm.entity.User;
import poly.edu.vantix_hrm.repository.RoleRepository;
import poly.edu.vantix_hrm.repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Bean
    CommandLineRunner initData() {
        return args -> {

            // ===== ROLE =====
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setRoleName("ADMIN");
                        return roleRepository.save(r);
                    });

            // ===== USER =====
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@vantix.com");
                admin.setPasswordHash(
                        passwordEncoder.encode("123456")
                );
                admin.setRole(adminRole);
                admin.setStatus(User.UserStatus.ACTIVE);

                userRepository.save(admin);

                System.out.println("✅ Admin account created");
            } else {
                System.out.println("ℹ️ Admin account already exists");
            }
        };
    }
}
