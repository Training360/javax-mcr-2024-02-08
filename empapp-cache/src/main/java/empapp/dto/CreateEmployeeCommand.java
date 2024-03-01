package empapp.dto;

import empapp.entity.Address;
import lombok.Data;

import java.util.List;

@Data
public class CreateEmployeeCommand {

    private String name;

    private List<Address> addresses;
}
