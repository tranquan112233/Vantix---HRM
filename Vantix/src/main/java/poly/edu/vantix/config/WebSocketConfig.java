package poly.edu.vantix.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import poly.edu.vantix.service.NotificationWebSocketService;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationWebSocketService notificationWebSocketService;

    public WebSocketConfig(NotificationWebSocketService notificationWebSocketService) {
        this.notificationWebSocketService = notificationWebSocketService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketService, "/ws/notifications")
                .addInterceptors(new SecurityContextHandshakeInterceptor())
                .setAllowedOriginPatterns("*");
    }

    private static class SecurityContextHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Map<String, Object> attributes
        ) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                attributes.put("authentication", authentication);
            }
            return true;
        }

        @Override
        public void afterHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Exception exception
        ) {
        }
    }
}
