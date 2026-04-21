package poly.edu.vantix.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import poly.edu.vantix.entity.SystemLog;
import poly.edu.vantix.entity.enums.LogLevel;
import poly.edu.vantix.repository.SystemLogRepository;
import poly.edu.vantix.security.JwtUserPrincipal;

import java.util.Locale;

@Component
public class SystemLogAuditInterceptor implements HandlerInterceptor {

    private final SystemLogRepository systemLogRepository;

    public SystemLogAuditInterceptor(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        if (!shouldLog(request)) {
            return;
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            JwtUserPrincipal principal = authentication != null
                    && authentication.getPrincipal() instanceof JwtUserPrincipal jwtPrincipal
                    ? jwtPrincipal
                    : null;

            SystemLog log = new SystemLog();
            log.setLevel(resolveLevel(response.getStatus(), ex));
            log.setActorUserId(principal == null ? null : principal.getId());
            log.setActorUsername(principal == null ? "anonymous" : principal.getUsername());
            log.setAction(resolveAction(request));
            log.setModule(resolveModule(request));
            log.setEntityName(resolveModule(request));
            log.setEntityId(resolveEntityId(request));
            log.setDescription(request.getMethod() + " " + request.getRequestURI() + " returned " + response.getStatus());
            log.setIpAddress(resolveClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
            systemLogRepository.save(log);
        } catch (RuntimeException ignored) {
            // Audit logging must not block the business request.
        }
    }

    private boolean shouldLog(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (!uri.startsWith("/api/")) {
            return false;
        }

        if (uri.startsWith("/api/system-logs") || uri.equals("/api/auth/me")) {
            return false;
        }

        return uri.equals("/api/auth/login") || !"GET".equalsIgnoreCase(method);
    }

    private LogLevel resolveLevel(int status, Exception ex) {
        if (ex != null || status >= 500) {
            return LogLevel.ERROR;
        }
        if (status >= 400) {
            return LogLevel.WARN;
        }
        return LogLevel.INFO;
    }

    private String resolveAction(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod().toUpperCase(Locale.ROOT);

        if (uri.equals("/api/auth/login")) {
            return "LOGIN";
        }

        return switch (method) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> method;
        };
    }

    private String resolveModule(HttpServletRequest request) {
        String[] parts = request.getRequestURI().split("/");
        if (parts.length < 3) {
            return "System";
        }

        String module = parts[2].replace("-", " ");
        String[] words = module.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1));
        }
        return result.isEmpty() ? "System" : result.toString();
    }

    private Long resolveEntityId(HttpServletRequest request) {
        String[] parts = request.getRequestURI().split("/");
        if (parts.length < 4) {
            return null;
        }

        try {
            return Long.valueOf(parts[3]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
