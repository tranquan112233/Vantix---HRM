package poly.edu.vantix_hrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // DÒNG NÀY LÀ QUAN TRỌNG NHẤT
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic: gửi cho nhiều người
        // /user: gửi cho 1 người cụ thể
        config.enableSimpleBroker("/queue", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-hr")
                .setAllowedOriginPatterns("http://localhost:5173,http://localhost:5174,http://localhost:5175,http://localhost:5176")
                .withSockJS();
    }
}