package ee.taltech.fooddeliveryapp.endpoint;

import ee.taltech.fooddeliveryapp.exceptions.InvalidTimeStampException;
import ee.taltech.fooddeliveryapp.service.DeliveryFeeCalculator;
import ee.taltech.fooddeliveryapp.exceptions.NoWeatherFoundException;
import ee.taltech.fooddeliveryapp.exceptions.UnknownCityException;
import ee.taltech.fooddeliveryapp.exceptions.UnknownVehicleException;
import ee.taltech.fooddeliveryapp.exceptions.VehicleForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/delivery")
public class DeliveryFeeController {

    private final DeliveryFeeCalculator calculator;
    private final FeeResponse response;

    @Autowired
    DeliveryFeeController(DeliveryFeeCalculator calculator, FeeResponse response) {
        this.calculator = calculator;
        this.response = response;
    }

    /**
     * This method calculates the delivery fee based on the provided FeeRequest object and returns a FeeResponse object.
     *
     * @param request FeeRequest object containing the city, vehicle type, and timestamp information for the delivery
     * @return a ResponseEntity containing the calculated delivery fee and an error message if an error occurs
     */
    @PostMapping("/fee")
    public ResponseEntity<FeeResponse> calculateFee(@RequestBody FeeRequest request) {
        try {
            BigDecimal deliveryFee = calculator.calculateFee(request.getCity(), request.getVehicleType(), request.getTimeStamp());
            response.setFee(deliveryFee);
            response.setErrorMessage(null);

            return ResponseEntity.ok(response);
        } catch (UnknownCityException e) {
            response.setFee(null);
            response.setErrorMessage("Unknown city: " + request.getCity());
            return ResponseEntity.badRequest().body(response);
        } catch (UnknownVehicleException e) {
            response.setFee(null);
            response.setErrorMessage("Unknown vehicle type: " + request.getVehicleType());
            return ResponseEntity.badRequest().body(response);
        } catch (VehicleForbiddenException e) {
            response.setFee(null);
            response.setErrorMessage("Usage of selected vehicle type is forbidden");
            return ResponseEntity.badRequest().body(response);
        } catch (InvalidTimeStampException e) {
            response.setFee(null);
            response.setErrorMessage("No valid weather data for selected time for city: " + request.getCity());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (NoWeatherFoundException e) {
            response.setFee(null);
            response.setErrorMessage("Database contains no weather data for city: " + request.getCity());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.setFee(null);
            response.setErrorMessage("An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
