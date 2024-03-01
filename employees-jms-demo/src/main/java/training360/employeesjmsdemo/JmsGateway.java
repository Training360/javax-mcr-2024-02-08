package training360.employeesjmsdemo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class JmsGateway implements CommandLineRunner {

    private JmsTemplate jmsTemplate;

    @Override
    public void run(String... args) throws Exception {
        jmsTemplate.convertAndSend("EmployeesDestination", new EmployeeMessage(1L, "HelloJMS"));
    }

//    @JmsListener(destination = "EmployeesDestination")
    public void handleMessage(EmployeeMessage message) {
        log.info("Message: {}", message);
    }


}
