package training.employeesdemo;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeesService {

    private List<Employee> employees =
            List.of(
                    new Employee(1L, "John Doe", 1980),
                    new Employee(2L, "Jane Doe", 1990),
                    new Employee(3L, "Jack Doe", 1990),
                    new Employee(4L, "Joe Doe", 1990));

    public List<Employee> employees() {
        return employees;
    }

    public Employee findEmployeeById(long id) {
        return employees.stream().filter(e -> e.getId() == id).findAny()
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found wtih id: %d".formatted(id)));
    }
}
