package training.employeesdemo;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Lazy
@AllArgsConstructor
public class EmployeesService {

    private ApplicationEventPublisher publisher;

    private static List<Employee> employees =
            List.of(
                    new Employee(1L, "John Doe", 1980),
                    new Employee(2L, "Jane Doe", 1990),
                    new Employee(3L, "Jack Doe", 1990),
                    new Employee(4L, "Joe Doe", 1990));

    @PostConstruct
    public void printDebugMessage() {
      log.info("Creating employee service");
    }

    public List<Employee> employees() {
        return employees;
    }

    public Employee findEmployeeById(long id) {
        var employee = employees.stream().filter(e -> e.getId() == id).findAny()
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found wtih id: %d".formatted(id)));
        publisher.publishEvent(new EmployeeHasBeenQueriedEvent(employee.getName()));
        return employee;
    }
}
