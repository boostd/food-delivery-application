package ee.taltech.fooddeliveryapp.endpoint;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Holds response data for a REST endpoint with a fee amount and error message fields.
 */
@Getter
@Setter
@Component
public class FeeResponse {
    private BigDecimal fee;
    private String errorMessage;
}
