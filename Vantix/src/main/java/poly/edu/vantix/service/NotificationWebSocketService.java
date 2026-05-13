package poly.edu.vantix.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import poly.edu.vantix.dto.response.NotificationResponse;
import poly.edu.vantix.security.JwtUserPrincipal;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationWebSocketService extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<Long, CopyOnWriteArrayList<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public NotificationWebSocketService() {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = currentUserId(session);
        if (userId == null || !canViewNotifications(session)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("NOTIFICATION_VIEW is required"));
            return;
        }

        sessions.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(session);
        session.sendMessage(jsonMessage("connected", Map.of("connected", true)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = currentUserId(session);
        if (userId != null) {
            remove(userId, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = currentUserId(session);
        if (userId != null) {
            remove(userId, session);
        }
        session.close(CloseStatus.SERVER_ERROR);
    }

    public void publish(Long userId, NotificationResponse notification) {
        if (userId == null || notification == null) {
            return;
        }

        List<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null || userSessions.isEmpty()) {
            return;
        }

        TextMessage message;
        try {
            message = jsonMessage("notification", serializableNotification(notification));
        } catch (JsonProcessingException error) {
            return;
        }

        for (WebSocketSession session : userSessions) {
            if (!session.isOpen()) {
                remove(userId, session);
                continue;
            }

            try {
                session.sendMessage(message);
            } catch (IOException | IllegalStateException error) {
                remove(userId, session);
            }
        }
    }

    private TextMessage jsonMessage(String type, Object payload) throws JsonProcessingException {
        return new TextMessage(objectMapper.writeValueAsString(Map.of(
                "type", type,
                "payload", payload
        )));
    }

    private Map<String, Object> serializableNotification(NotificationResponse notification) {
        return Map.ofEntries(
                Map.entry("id", notification.getId()),
                Map.entry("userId", notification.getUserId()),
                Map.entry("type", valueOrEmpty(notification.getType())),
                Map.entry("title", valueOrEmpty(notification.getTitle())),
                Map.entry("message", valueOrEmpty(notification.getMessage())),
                Map.entry("titleKey", valueOrEmpty(notification.getTitleKey())),
                Map.entry("messageKey", valueOrEmpty(notification.getMessageKey())),
                Map.entry("messageParams", notification.getMessageParams() == null ? Map.of() : notification.getMessageParams()),
                Map.entry("targetUrl", valueOrEmpty(notification.getTargetUrl())),
                Map.entry("status", valueOrEmpty(notification.getStatus())),
                Map.entry("readAt", timeOrNull(notification.getReadAt())),
                Map.entry("createdAt", timeOrNull(notification.getCreatedAt()))
        );
    }

    private Object timeOrNull(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private Long currentUserId(WebSocketSession session) {
        Authentication authentication = currentAuthentication(session);
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal userPrincipal)) {
            return null;
        }

        return userPrincipal.getId();
    }

    private boolean canViewNotifications(WebSocketSession session) {
        Authentication authentication = currentAuthentication(session);
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("NOTIFICATION_VIEW"::equals);
    }

    private Authentication currentAuthentication(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal instanceof Authentication authentication) {
            return authentication;
        }

        Object authentication = session.getAttributes().get("authentication");
        return authentication instanceof Authentication value ? value : null;
    }

    private void remove(Long userId, WebSocketSession session) {
        List<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return;
        }

        userSessions.remove(session);
        if (userSessions.isEmpty()) {
            sessions.remove(userId);
        }
    }
}
