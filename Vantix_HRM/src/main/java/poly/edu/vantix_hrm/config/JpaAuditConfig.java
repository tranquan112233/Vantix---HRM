package poly.edu.vantix_hrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/*
 * JpaAuditConfig
 * -------------------------------------------
 * Bật tính năng JPA Auditing
 * Tự động set createdBy, updatedBy, createdAt, updatedAt khi lưu Entity
 *
 * auditorAwareRef trỏ tới bean AuditorAwareImpl
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {
}