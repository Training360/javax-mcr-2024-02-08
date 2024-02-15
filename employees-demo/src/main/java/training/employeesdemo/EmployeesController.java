package training.employeesdemo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class EmployeesController {

    private final EmployeesService employeesService;

    @GetMapping("/employees")
    public List<Employee> employees() {
            return employeesService.employees()
                    .stream().peek(e -> e.setName(e.getName().toUpperCase())).toList();
    }

    @GetMapping("/employees/{id}")
    public Employee findEmployeeById(@PathVariable("id") long id) {
        return employeesService.findEmployeeById(id);
    }
}
