package poly.edu.vantix_hrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VantixHrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(VantixHrmApplication.class, args);
    }

}
