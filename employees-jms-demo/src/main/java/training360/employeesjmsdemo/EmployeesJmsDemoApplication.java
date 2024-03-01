package training360.employeesjmsdemo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

import java.util.Map;

@SpringBootApplication
public class EmployeesJmsDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeesJmsDemoApplication.class, args);
	}

	@Bean
	public MessageConverter messageConverter() {
		MappingJackson2MessageConverter converter
				= new MappingJackson2MessageConverter();
		converter.setTypeIdPropertyName("_typeId");
		converter.setTypeIdMappings(
				Map.of("employee", EmployeeMessage.class));

		return converter;
	}

}
