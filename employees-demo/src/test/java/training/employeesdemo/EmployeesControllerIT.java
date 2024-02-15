package training.employeesdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EmployeesControllerIT {

	@Autowired
	EmployeesController employeesController;

	@Autowired
	EmployeesService employeesService;

	@Test
	void employees() {
		var employees = employeesController.employees();
		assertThat(employees)
				.extracting(Employee::getName).
				contains("JOHN DOE");
	}

	@Test
	void service() {
		var employees = employeesService.employees();
		assertThat(employees)
				.extracting(Employee::getName).
				contains("JOHN DOE");
	}

}
