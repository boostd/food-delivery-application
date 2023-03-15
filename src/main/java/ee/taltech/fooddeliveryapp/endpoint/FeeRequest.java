package ee.taltech.fooddeliveryapp.endpoint;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeeRequest {
    private String city;
    private String vehicleType;
}
