package ee.taltech.fooddeliveryapp.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import ee.taltech.fooddeliveryapp.config.WeatherDataConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.exceptions.*;

public class DeliveryFeeCalculatorTest {
    @Mock
    private WeatherDataService weatherDataService;

    @InjectMocks
    private DeliveryFeeCalculator deliveryFeeCalculator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests the calculateFee method with valid inputs, expecting a successful fee calculation.
     */
    @Test
    public void testCalculateFeeWithValidInputs() throws InvalidTimeStampException, VehicleForbiddenException,
            NoWeatherFoundException, UnknownVehicleException, UnknownCityException {
        // Arrange
        String city = "tallinn";
        String vehicleType = "scooter";
        LocalDateTime timeStamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(1641045600)
                , ZoneOffset.UTC);

        WeatherData weatherData = new WeatherData();
        weatherData.setAirTemperature(10.0);
        weatherData.setWindSpeed(5.0);
        weatherData.setWeatherPhenomenon("Clear");
        weatherData.setTimeStamp(1641045500L);

        when(weatherDataService.getWeatherDataByTimeStamp(anyInt(), anyLong(), anyLong()))
                .thenReturn(Collections.singletonList(weatherData));

        // Act
        BigDecimal result = deliveryFeeCalculator.calculateFee(city, vehicleType, timeStamp);

        // Assert
        BigDecimal expectedFee = new BigDecimal("3.5");
        assert result.equals(expectedFee);
    }

    /**
     * Tests the calculateFee method with an unknown city, expecting an UnknownCityException.
     */
    @Test
    public void testCalculateFeeWithUnknownCityException() {
        // Arrange
        String city = "paris";
        String vehicleType = "bike";
        LocalDateTime timeStamp = LocalDateTime.of(2022, 1, 1, 10, 0);

        // Act & Assert
        assertThrows(UnknownCityException.class, () -> {
            deliveryFeeCalculator.calculateFee(city, vehicleType, timeStamp);
        });
    }

    /**
     * Tests the calculateFee method with an unknown vehicle type, expecting an UnknownVehicleException.
     */
    @Test
    public void testCalculateFeeWithUnknownVehicleException() {
        // Arrange
        String city = "tallinn";
        String vehicleType = "airplane";
        LocalDateTime timeStamp = LocalDateTime.of(2022, 1, 1, 10, 0);

        // Act & Assert
        assertThrows(UnknownVehicleException.class, () -> {
            deliveryFeeCalculator.calculateFee(city, vehicleType, timeStamp);
        });
    }

    /**
     * Tests the calculateFee method with no weather data found, expecting a NoWeatherFoundException.
     */
    @Test
    public void testCalculateFeeWithNoWeatherFoundException() {
        // Arrange
        String city = "tallinn";
        String vehicleType = "bike";

        when(weatherDataService.getWeatherDataByTimeStamp(anyInt(), anyLong(), anyLong())).thenReturn(null);

        // Act & Assert
        assertThrows(NoWeatherFoundException.class, () -> {
            deliveryFeeCalculator.calculateFee(city, vehicleType, null);
        });
    }

    /**
     * Tests the calculateFee method with vehicle usage forbidden due to weather conditions,
     * expecting a VehicleForbiddenException.
     */
    @Test
    public void testCalculateFeeWithVehicleForbiddenException() {
        // Arrange
        String city = "tartu";
        String vehicleType = "bike";

        WeatherData weatherData = new WeatherData();
        weatherData.setAirTemperature(-15.0);
        weatherData.setWmoCode(WeatherDataConstants.WMO_CODES[1]);
        weatherData.setWindSpeed(30.0);
        weatherData.setWeatherPhenomenon("glaze");
        weatherData.setTimeStamp(1641042000L);
        when(weatherDataService.getLatestWeatherData(anyInt())).thenReturn(weatherData);

        // Act & Assert
        assertThrows(VehicleForbiddenException.class, () -> {
            deliveryFeeCalculator.calculateFee(city, vehicleType, null);
        });
    }

    /**
     * Tests the calculateFee method with an invalid timestamp, expecting an InvalidTimeStampException.
     */
    @Test
    public void testCalculateFeeWithInvalidTimeStampException() {
        // Arrange
        String city = "pärnu";
        String vehicleType = "car";
        LocalDateTime timeStamp = LocalDateTime.of(2022, 1, 1, 10, 0);

        WeatherData weatherData = new WeatherData();
        weatherData.setAirTemperature(-5.0);
        weatherData.setWindSpeed(10.0);
        weatherData.setWeatherPhenomenon("Clear");
        weatherData.setTimeStamp(1641052800L); // Timestamp for 11:00, not 10:00

        when(weatherDataService.getWeatherDataByTimeStamp(anyInt(), anyLong(), anyLong()))
                .thenReturn(null);

        // Act & Assert
        assertThrows(InvalidTimeStampException.class, () -> {
            deliveryFeeCalculator.calculateFee(city, vehicleType, timeStamp);
        });
    }

    /**
     * Tests the calculateFee method with the latest weather data, expecting a successful fee calculation.
     */
    @Test
    public void testCalculateFeeWithLatestWeatherData() throws InvalidTimeStampException,
            VehicleForbiddenException, NoWeatherFoundException, UnknownVehicleException, UnknownCityException {
        // Arrange
        String city = "tallinn";
        String vehicleType = "scooter";
        LocalDateTime timeStamp = null; // null timestamp means the latest weather data is used

        WeatherData weatherData = new WeatherData();
        weatherData.setAirTemperature(5.0);
        weatherData.setWindSpeed(15.0);
        weatherData.setWeatherPhenomenon("Rain");
        weatherData.setWmoCode(WeatherDataConstants.WMO_CODES[0]);
        weatherData.setTimeStamp(1641042000L);

        when(weatherDataService.getLatestWeatherData(anyInt())).thenReturn(weatherData);

        // Act
        BigDecimal result = deliveryFeeCalculator.calculateFee(city, vehicleType, timeStamp);

        // Assert
        BigDecimal expectedFee = new BigDecimal("4.0");
        assert result.equals(expectedFee);
    }
}