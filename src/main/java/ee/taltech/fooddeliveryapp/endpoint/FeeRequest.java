package ee.taltech.fooddeliveryapp.endpoint;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a request data for a REST endpoint with an optional timestamp field.
 */
@Getter
@Setter
public class FeeRequest {
    private String city;
    private String vehicleType;
    private LocalDateTime timeStamp;
}
