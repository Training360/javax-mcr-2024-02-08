package training.employeesdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class EmployeesControllerMockIT {

    @MockBean
    EmployeesService employeesService;

    @Autowired
    EmployeesController employeesController;

    @Test
    void employees() {
        when(employeesService.employees()).thenReturn(List.of(new Employee(1L, "Goofy", 2000)));
        var employees = employeesController.employees();
        assertThat(employees)
                .extracting(Employee::getName)
                .containsExactly("GOOFY");
    }
}
