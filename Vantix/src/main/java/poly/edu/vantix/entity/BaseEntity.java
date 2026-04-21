package poly.edu.vantix.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/*
 * BaseEntity
 * - Entity cha dùng chung cho toàn hệ thống
 * - Chứa id, audit và soft delete
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    // Khóa chính
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thời gian tạo
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Id user tạo bản ghi
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    // Thời gian cập nhật gần nhất
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Id user cập nhật gần nhất
    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    // Đánh dấu xóa mềm
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    // Thời gian xóa mềm
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Id user thực hiện xóa mềm
    @Column(name = "deleted_by")
    private Long deletedBy;
}