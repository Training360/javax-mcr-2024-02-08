package training.employeesdemo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;

@ControllerAdvice
public class EmployeeExceptionHandlerAdvice {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ProblemDetail handleNotFoundException(EmployeeNotFoundException e) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        detail.setTitle("Not found");
        detail.setProperty("id", UUID.randomUUID().toString());
        return detail;
    }

}
