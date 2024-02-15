package employees;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class EmployeesRepository {

    private AtomicLong idGenerator = new AtomicLong();

    private List<Employee> employees = Collections.synchronizedList(new ArrayList<>(List.of(
            new Employee(idGenerator.incrementAndGet(), "John Doe"),
            new Employee(idGenerator.incrementAndGet(), "Jack Doe")
    )));



    public List<Employee> findAll() {
        return new ArrayList<>(employees);
    }

    public Optional<Employee> findById(long id) {
        return employees.stream().filter(employee -> employee.getId() == id).findAny();
    }

    public void deleteById(long id) {
        employees.removeIf(employee -> employee.getId() == id);
    }

    public void save(Employee employee) {
        employee.setId(idGenerator.incrementAndGet());
        employees.add(employee);
    }
}
