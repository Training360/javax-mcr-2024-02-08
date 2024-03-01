package training.employeesdemo;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableConfigurationProperties(HelloProperties.class)
@AllArgsConstructor
public class HelloController {

    private HelloProperties helloProperties;

    @GetMapping("/api/hello")
    public String hello() {
        return helloProperties.getWelcomeMessage();
    }
}
