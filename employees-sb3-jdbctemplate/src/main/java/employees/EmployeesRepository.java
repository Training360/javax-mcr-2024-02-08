package employees;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class EmployeesRepository {

    private JdbcTemplate jdbcTemplate;

    public List<EmployeeResource> findAllResources() {
        return jdbcTemplate.query("select id, emp_name from employees",
                EmployeesRepository::mapToEmployeeResource);

    }

    public Optional<Employee> findById(long id) {
        return Optional.ofNullable(jdbcTemplate.queryForObject("select id, emp_name from employees where id = ?",
                EmployeesRepository::mapToEmployee,
                id));

    }

    public void save(Employee employee) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into employees(emp_name) values (?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, employee.getName());
            return ps;
        }, keyHolder);
        employee.setId((Long) keyHolder.getKeys().get("id"));
    }

    public void deleteById(long id) {
        jdbcTemplate.update("delete from employees where id = ?", id);
    }

    private static EmployeeResource mapToEmployeeResource(ResultSet resultSet, int i) throws SQLException {
        long id = resultSet.getLong("id");
        String name = resultSet.getString("emp_name");
        var employee = new EmployeeResource(id, name);
        return employee;
    }

    private static Employee mapToEmployee(ResultSet resultSet, int i) throws SQLException {
        long id = resultSet.getLong("id");
        String name = resultSet.getString("emp_name");
        var employee = new Employee(id, name);
        return employee;
    }


    public void update(Employee employee) {
        jdbcTemplate.update("update employees set emp_name = ? where id = ?",
                employee.getName(), employee.getId());
    }
}
