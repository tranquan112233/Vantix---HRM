package poly.edu.vantix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import poly.edu.vantix.entity.enums.LogLevel;

/*
 * SystemLog
 * - Ghi log hệ thống để theo dõi thao tác
 * - Dùng cho audit, debug, truy vết
 */
@Getter
@Setter
@Entity
@Table(name = "system_logs")
public class SystemLog extends BaseEntity {

    // Mức độ log: INFO, WARN, ERROR
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 10)
    private LogLevel level = LogLevel.INFO;

    // Id user thực hiện thao tác
    @Column(name = "actor_user_id")
    private Long actorUserId;

    // Username tại thời điểm ghi log
    @Column(name = "actor_username", length = 50)
    private String actorUsername;

    // Hành động thực hiện
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    // Module liên quan
    @Column(name = "module", length = 100)
    private String module;

    // Tên entity bị tác động
    @Column(name = "entity_name", length = 100)
    private String entityName;

    // Id bản ghi bị tác động
    @Column(name = "entity_id")
    private Long entityId;

    // Nội dung chi tiết log
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // IP client
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    // Thông tin trình duyệt / thiết bị
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
}