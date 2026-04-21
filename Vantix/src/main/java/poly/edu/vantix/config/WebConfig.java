package poly.edu.vantix.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SystemLogAuditInterceptor systemLogAuditInterceptor;

    public WebConfig(SystemLogAuditInterceptor systemLogAuditInterceptor) {
        this.systemLogAuditInterceptor = systemLogAuditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(systemLogAuditInterceptor).addPathPatterns("/api/**");
    }
}
