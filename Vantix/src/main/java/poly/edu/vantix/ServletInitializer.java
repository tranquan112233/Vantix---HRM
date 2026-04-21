package poly.edu.vantix;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/*
 * Hỗ trợ deploy backend dạng WAR lên servlet container bên ngoài.
 */
public class ServletInitializer extends SpringBootServletInitializer {

    /*
     * Khai báo class nguồn khi container nạp WAR.
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(VantixApplication.class);
    }
}
