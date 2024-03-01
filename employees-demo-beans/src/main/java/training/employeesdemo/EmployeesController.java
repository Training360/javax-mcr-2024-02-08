package training.employeesdemo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
@AllArgsConstructor
@Lazy
public class EmployeesController {

    private  EmployeesService employeesService;


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
