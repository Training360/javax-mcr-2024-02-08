package training360.employeesclient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

@SpringBootApplication
@Slf4j
@AllArgsConstructor
public class EmployeesClientApplication implements CommandLineRunner {

	private RestClient.Builder restClientBuilder;

	public static void main(String[] args) {
		SpringApplication.run(EmployeesClientApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Hello World");

		var restClient = restClientBuilder
				.baseUrl("http://localhost:8080")
				.build();

//		var response = restClient
//				.get()
//				.uri("/api/employees")
//				.retrieve()
//				.body(new ParameterizedTypeReference<List<EmployeeResource>>() {});


		var factory = HttpServiceProxyFactory
				.builderFor(RestClientAdapter.create(restClient)).build();
		var client = factory.createClient(EmployeeGateway.class);
		var response = client.listEmployees();

		log.info("Response from server: {}", response);

	}
}
