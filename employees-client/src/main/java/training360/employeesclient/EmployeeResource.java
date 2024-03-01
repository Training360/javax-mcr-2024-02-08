package training360.employeesclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResource {

    private Long id;

    private String name;

    public EmployeeResource(String name) {
        this.name = name;
    }
}
