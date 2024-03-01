package training360.employeesclient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/api/employees")
public interface EmployeeGateway {

    @GetExchange
    List<EmployeeResource> listEmployees();
}
