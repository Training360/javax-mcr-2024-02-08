package training.employeesdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoggerService {

    @EventListener
    public void handleEvent(EmployeeHasBeenQueriedEvent e) {
        log.info("Handle: {}", e);
    }
}
