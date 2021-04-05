package velocorner.info;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    @RequestMapping("/")
    public String index() {
        return "<h1>Hello</h1>";
    }
}
