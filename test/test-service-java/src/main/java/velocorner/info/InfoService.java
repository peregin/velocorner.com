package velocorner.info;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InfoService {

    public static void main(String... args) {
        SpringApplication app = new SpringApplication(InfoService.class);
        app.run(InfoService.class, args);
    }
}
