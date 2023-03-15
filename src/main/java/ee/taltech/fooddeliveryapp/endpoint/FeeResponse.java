package ee.taltech.fooddeliveryapp.endpoint;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
public class FeeResponse {
    private BigDecimal fee;
    private String errorMessage;
}
