package poly.edu.vantix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
 * Class khởi động chính của backend Vantix.
 */
@SpringBootApplication
@EnableScheduling
public class VantixApplication {

    /*
     * Điểm vào khi chạy ứng dụng bằng IDE, Maven hoặc file WAR.
     */
    public static void main(String[] args) {
        SpringApplication.run(VantixApplication.class, args);
    }
}
