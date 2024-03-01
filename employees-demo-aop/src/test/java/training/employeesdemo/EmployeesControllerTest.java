package training.employeesdemo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeesControllerTest {

    @Mock
    EmployeesService employeesService;

    @InjectMocks
    EmployeesController employeesController;

    @Test
    void employees() {
        when(employeesService.employees()).thenReturn(
                 List.of(new Employee(1L, "John Doe", 1980),
                        new Employee(2L, "Jane Doe", 1990)
                ));

        var employees = employeesController.employees();

//        assertEquals(List.of(new Employee("John Doe", 1980),
//                new Employee("Jane Doe", 1990)), employees);

        assertThat(employees)
                .extracting(Employee::getName)
                .containsExactly("JOHN DOE", "JANE DOE");
    }

    @Test
    void findEmployeeById() {
        employeesController.findEmployeeById(10);

        verify(employeesService).findEmployeeById(longThat(i -> i == 10));
    }

}