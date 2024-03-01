package empapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
public class AddressDto implements Serializable {

    private Long id;

    private String city;
}
