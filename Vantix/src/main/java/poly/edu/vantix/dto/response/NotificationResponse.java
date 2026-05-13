package poly.edu.vantix.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import poly.edu.vantix.entity.Notification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, String>> STRING_MAP_TYPE = new TypeReference<>() {};

    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String message;
    private String titleKey;
    private String messageKey;
    private Map<String, String> messageParams;
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
                .titleKey(n.getTitleKey())
                .messageKey(n.getMessageKey())
                .messageParams(parseParams(n.getMessageParams()))
                .targetUrl(n.getTargetUrl())
                .status(n.getStatus() != null ? n.getStatus().name() : null)
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private static Map<String, String> parseParams(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(raw, STRING_MAP_TYPE);
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }
}
