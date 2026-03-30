package poly.edu.vantix_hrm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/*
 * BaseEntity
 * -------------------------------------------
 * Class cha cho tất cả Entity trong hệ thống
 *
 * Tự động ghi:
 *   - createdAt  : thời gian tạo
 *   - updatedAt  : thời gian cập nhật gần nhất
 *   - createdBy  : userId tạo
 *   - updatedBy  : userId cập nhật gần nhất
 *   - deleted    : xóa mềm (true = đã xóa)
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;

    @LastModifiedBy
    private Long updatedBy;

    // Xóa mềm — true = đã xóa, false = còn tồn tại
    private Boolean deleted = false;
}