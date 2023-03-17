package ee.taltech.fooddeliveryapp.endpoint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a request data for a REST endpoint with an optional timestamp field.
 */
@Getter
@Setter
@AllArgsConstructor
public class FeeRequest {
    private String city;
    private String vehicleType;
    private LocalDateTime timeStamp;
}
