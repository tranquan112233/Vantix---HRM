package poly.edu.vantix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import poly.edu.vantix.security.JwtAuditorAware;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new JwtAuditorAware();
    }
}
