package ee.taltech.fooddeliveryapp.endpoint;

import ee.taltech.fooddeliveryapp.service.DeliveryFeeCalculator;
import ee.taltech.fooddeliveryapp.service.NoWeatherFoundException;
import ee.taltech.fooddeliveryapp.service.UnknownCityException;
import ee.taltech.fooddeliveryapp.service.UnknownVehicleException;
import ee.taltech.fooddeliveryapp.service.VehicleForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Component
public class DeliveryFeeController {
    @RestController
    @RequestMapping("/delivery")
    public static class FeeController {

        @Autowired
        private DeliveryFeeCalculator calculator;
        @Autowired
        private FeeResponse response;

        @PostMapping("/fee")
        public ResponseEntity<FeeResponse> calculateFee(@RequestBody FeeRequest request) {
            try {
                BigDecimal deliveryFee = calculator.calculateFee(request.getCity(), request.getVehicleType());
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
            } catch (NoWeatherFoundException e) {
                response.setFee(null);
                response.setErrorMessage("Database contains no weather data for city: " + request.getCity());
                return ResponseEntity.internalServerError().body(response);
            } catch (Exception e) {
                response.setFee(null);
                response.setErrorMessage("An unexpected error occurred");
                return ResponseEntity.internalServerError().body(response);
            }
        }
    }

}
