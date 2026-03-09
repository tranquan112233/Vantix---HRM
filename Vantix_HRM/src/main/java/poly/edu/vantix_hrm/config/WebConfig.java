package poly.edu.vantix_hrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ URL: http://localhost:8080/avatars/... -> Thư mục vật lý uploads/avatars/
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:uploads/avatars/");
    }
}