package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.SystemLog;
import poly.edu.vantix.entity.enums.LogLevel;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogResponse {

    private Long id;

    private LogLevel level;

    private Long actorUserId;

    private String actorUsername;

    private String action;

    private String module;

    private String entityName;

    private Long entityId;

    private String description;

    private String ipAddress;

    private String userAgent;

    private LocalDateTime createdAt;

    public static SystemLogResponse fromEntity(SystemLog log) {
        return SystemLogResponse.builder()
                .id(log.getId())
                .level(log.getLevel())
                .actorUserId(log.getActorUserId())
                .actorUsername(log.getActorUsername())
                .action(log.getAction())
                .module(log.getModule())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
