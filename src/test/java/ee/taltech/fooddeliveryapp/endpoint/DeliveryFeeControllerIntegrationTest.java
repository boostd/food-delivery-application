package ee.taltech.fooddeliveryapp.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.fooddeliveryapp.exceptions.InvalidTimeStampException;
import ee.taltech.fooddeliveryapp.exceptions.NoWeatherFoundException;
import ee.taltech.fooddeliveryapp.exceptions.UnknownCityException;
import ee.taltech.fooddeliveryapp.exceptions.UnknownVehicleException;
import ee.taltech.fooddeliveryapp.exceptions.VehicleForbiddenException;
import ee.taltech.fooddeliveryapp.service.DeliveryFeeCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DeliveryFeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private DeliveryFeeCalculator calculator;

    /**
     * Tests the case when a valid FeeRequest is provided, and the delivery fee is calculated successfully.
     * The response should contain the calculated delivery fee and no error message.
     */
    @Test
    void calculateFee_validRequest_returnsFeeResponse()
            throws Exception, InvalidTimeStampException, VehicleForbiddenException,
            NoWeatherFoundException, UnknownVehicleException, UnknownCityException {
        // Arrange
        FeeRequest request = new FeeRequest("Tallinn", "Car", LocalDateTime.now());
        String requestJson = objectMapper.writeValueAsString(request);
        when(calculator.calculateFee(any(), any(), any())).thenReturn(new BigDecimal("2.0"));

        // Act
        MvcResult result = mockMvc.perform(post("/delivery/fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        FeeResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), FeeResponse.class);
        assertThat(response.getFee()).isNotNull();
        assertThat(response.getErrorMessage()).isNull();
    }

    /**
     * Tests the case when an unknown city is provided in the FeeRequest.
     * The response should have a bad request status and an error message indicating the unknown city.
     */
    @Test
    void calculateFee_unknownCity_returnsBadRequest()
            throws Exception, InvalidTimeStampException, VehicleForbiddenException,
            NoWeatherFoundException, UnknownVehicleException, UnknownCityException {
        // Arrange
        FeeRequest request = new FeeRequest("UnknownCity", "Car", LocalDateTime.now());
        String requestJson = objectMapper.writeValueAsString(request);
        when(calculator.calculateFee(any(), any(), any())).thenThrow(new UnknownCityException("No weather for Tallinn for requested time found in database!"));

        // Act
        MvcResult result = mockMvc.perform(post("/delivery/fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Assert
        FeeResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), FeeResponse.class);
        assertThat(response.getFee()).isNull();
        assertThat(response.getErrorMessage()).isEqualTo("Unknown city: UnknownCity");
    }

    /**
     * Tests the case when an invalid timestamp is provided in the FeeRequest.
     * The response should have a not found status and an error message indicating no valid weather data.
     */
    @Test
    void calculateFee_invalidTimestamp_returnsBadRequest()
            throws InvalidTimeStampException, VehicleForbiddenException,
            NoWeatherFoundException, UnknownVehicleException, UnknownCityException, Exception {
        // Arrange
        FeeRequest request = new FeeRequest("Tallinn", "Car", LocalDateTime.of(2020, 1, 1, 0, 0));
        String requestJson = objectMapper.writeValueAsString(request);
        when(calculator.calculateFee(any(), any(), any())).thenThrow(new InvalidTimeStampException("No weather for Tallinn for requested time found in database!"));

        // Act
        MvcResult result = mockMvc.perform(post("/delivery/fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        FeeResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), FeeResponse.class);
        assertThat(response.getFee()).isNull();
        assertThat(response.getErrorMessage())
                .isEqualTo("No valid weather data for selected time for city: Tallinn");
    }

    /**
     * Tests the case when an unknown vehicle type is provided in the FeeRequest.
     * The response should have a bad request status and an error message indicating the unknown vehicle type.
     */
    @Test
    void calculateFee_unknownVehicle_returnsBadRequest()
            throws Exception, InvalidTimeStampException, VehicleForbiddenException,
            NoWeatherFoundException, UnknownVehicleException, UnknownCityException {
        // Arrange
        FeeRequest request = new FeeRequest("Tallinn", "UnknownVehicle", LocalDateTime.now());
        String requestJson = objectMapper.writeValueAsString(request);
        when(calculator.calculateFee(any(), any(), any())).thenThrow(new UnknownVehicleException("Unknown vehicle type!"));

        // Act
        MvcResult result = mockMvc.perform(post("/delivery/fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Assert
        FeeResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), FeeResponse.class);
        assertThat(response.getFee()).isNull();
        assertThat(response.getErrorMessage()).isEqualTo("Unknown vehicle type: UnknownVehicle");
    }

    /**
     * Tests the case when a forbidden vehicle type is provided in the FeeRequest.
     * The response should have a bad request status and an error message indicating the usage of the selected vehicle type is forbidden.
     */
    @Test
    void calculateFee_forbiddenVehicle_returnsBadRequest()
            throws Exception, InvalidTimeStampException, VehicleForbiddenException,
            NoWeatherFoundException, UnknownVehicleException, UnknownCityException {
        // Arrange
        FeeRequest request = new FeeRequest("Tallinn", "Bicycle", LocalDateTime.now());
        String requestJson = objectMapper.writeValueAsString(request);
        when(calculator.calculateFee(any(), any(), any())).thenThrow(new VehicleForbiddenException("Usage of selected vehicle type is forbidden!"));

        // Act
        MvcResult result = mockMvc.perform(post("/delivery/fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Assert
        FeeResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), FeeResponse.class);
        assertThat(response.getFee()).isNull();
        assertThat(response.getErrorMessage()).isEqualTo("Usage of selected vehicle type is forbidden");
    }

    /**
     * Tests the case when no weather data is found for the city provided in the FeeRequest.
     * The response should have a not found status and an error message indicating no weather data for the city.
     */
    @Test
    void calculateFee_noWeatherFound_returnsInternalServerError()
            throws Exception, InvalidTimeStampException, VehicleForbiddenException,
            NoWeatherFoundException, UnknownVehicleException, UnknownCityException {
        // Arrange
        FeeRequest request = new FeeRequest("Tallinn", "Car", LocalDateTime.now());
        String requestJson = objectMapper.writeValueAsString(request);
        when(calculator.calculateFee(any(), any(), any())).thenThrow(new NoWeatherFoundException("No weather found in database!"));

        // Act
        MvcResult result = mockMvc.perform(post("/delivery/fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andReturn();

        // Assert
        FeeResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), FeeResponse.class);
        assertThat(response.getFee()).isNull();
        assertThat(response.getErrorMessage()).isEqualTo("Database contains no weather data for city: Tallinn");
    }

    /**
     * Tests the case when an unexpected exception occurs during the delivery fee calculation.
     * The response should have an internal server error status and an error message indicating an unexpected error occurred.
     */
    @Test
    void calculateFee_unexpectedException_returnsInternalServerError()
            throws Exception, InvalidTimeStampException, VehicleForbiddenException,
            NoWeatherFoundException, UnknownVehicleException, UnknownCityException {
        // Arrange
        FeeRequest request = new FeeRequest("Tallinn", "Car", LocalDateTime.now());
        String requestJson = objectMapper.writeValueAsString(request);
        when(calculator.calculateFee(any(), any(), any())).thenThrow(new RuntimeException("Unexpected exception"));

        // Act
        MvcResult result = mockMvc.perform(post("/delivery/fee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andReturn();

        // Assert
        FeeResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), FeeResponse.class);
        assertThat(response.getFee()).isNull();
        assertThat(response.getErrorMessage()).isEqualTo("An unexpected error occurred");
    }

}
