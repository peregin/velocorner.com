package velocorner.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserService {

    public static void main(String... args) {
        SpringApplication app = new SpringApplication(UserService.class);
        app.run(UserService.class, args);
    }
}
