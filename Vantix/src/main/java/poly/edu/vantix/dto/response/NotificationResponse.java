package poly.edu.vantix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Notification;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String message;
    private String targetUrl;
    private String status;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUser() != null ? n.getUser().getId() : null)
                .type(n.getType() != null ? n.getType().name() : null)
                .title(n.getTitle())
                .message(n.getMessage())
                .targetUrl(n.getTargetUrl())
                .status(n.getStatus() != null ? n.getStatus().name() : null)
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
