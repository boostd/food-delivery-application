package ee.taltech.fooddeliveryapp.service;

import ee.taltech.fooddeliveryapp.common.WMOCodes;
import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.database.WeatherDataService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class DeliveryFeeCalculator {
    @Autowired
    WeatherDataService weatherDataService;

    private final HashMap<String, BigDecimal> cityFees = new HashMap<>() {{
        put("tallinn", BigDecimal.ONE);
        put("tartu", new BigDecimal("0.5"));
        put("pärnu", BigDecimal.ZERO);
    }};
    private final HashMap<String, BigDecimal> vehicleFees = new HashMap<>() {{
        put("car", BigDecimal.ONE);
        put("scooter", new BigDecimal("0.5"));
        put("bike", BigDecimal.ZERO);
    }};
    private final HashMap<String, Integer> wmoCodes = new HashMap<>() {{
        put("tallinn", WMOCodes.TALLINN_HARKU);
        put("tartu-tõravere", WMOCodes.TARTU_TORAVERE);
        put("pärnu", WMOCodes.PARNU);
    }};
    private final ArrayList<String> cityList = new ArrayList<>(Arrays.asList("tallinn", "tartu", "pärnu"));
    private final ArrayList<String> vehicleTypeList = new ArrayList<>(Arrays.asList("car", "scooter", "bike"));


    public BigDecimal calculateFee(String city, String vehicleType)
            throws UnknownCityException, UnknownVehicleException, NoWeatherFoundException, VehicleForbiddenException {
        city = city.toLowerCase();
        vehicleType = vehicleType.toLowerCase();

        if (!cityList.contains(city)) {
            throw new UnknownCityException("No such city found!");
        } else if (!vehicleTypeList.contains(vehicleType)) {
            throw new UnknownVehicleException("No such vehicle found!");
        }

        BigDecimal baseFee = calculateBaseFee(city, vehicleType);
        BigDecimal weatherFee = calculateWeatherFee(city, vehicleType);

        return baseFee.add(weatherFee);
    }

    private BigDecimal calculateBaseFee(String city, String vehicleType) {
        BigDecimal fee = new BigDecimal("2.0");
        return fee.add(cityFees.get(city)).add(vehicleFees.get(vehicleType));
    }

    private BigDecimal calculateWeatherFee(String city, String vehicleType)
            throws NoWeatherFoundException, VehicleForbiddenException {
        WeatherData data;

        try {
            data = fetchWeatherData(city);
        } catch (NoWeatherFoundException e) {
            throw e;
        }

        Double airTemperature = data.getAirTemperature();
        Double windSpeed = data.getWindSpeed();
        String phenomenon = data.getWeatherPhenomenon();

        BigDecimal airTemperatureFee;
        BigDecimal windSpeedFee;
        BigDecimal phenomenonFee;

        try {
            airTemperatureFee = calculateAirTemperatureFee(vehicleType, airTemperature);
            windSpeedFee = calculateWindSpeedFee(vehicleType, windSpeed);
            phenomenonFee = calculatePhenomenonFee(vehicleType, phenomenon);
        } catch (VehicleForbiddenException e) {
            throw e;
        }

        return airTemperatureFee.add(windSpeedFee).add(phenomenonFee);
    }

    private BigDecimal calculateAirTemperatureFee(String vehicleType, Double airTemperature) {
        if (vehicleType.equals("car")) {
            return BigDecimal.ZERO;
        }

        if (airTemperature < 0 && airTemperature >= -10) {
            return new BigDecimal("0.5");
        } else if (airTemperature < -10) {
            return new BigDecimal("1.0");
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculateWindSpeedFee(String vehicleType, Double windSpeed) throws VehicleForbiddenException {
        if (!vehicleType.equals("bike")) {
            return BigDecimal.ZERO;
        }

        if (windSpeed > 20) {
            throw new VehicleForbiddenException("Usage of selected vehicle type is forbidden");
        } else if (windSpeed > 10) {
            return new BigDecimal("0.5");
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculatePhenomenonFee(String vehicleType, String phenomenon) throws VehicleForbiddenException {
        if (vehicleType.equals("car")) {
            return BigDecimal.ZERO;
        }

        if (phenomenon.equalsIgnoreCase("glaze") || phenomenon.equalsIgnoreCase("hail")
                || phenomenon.toLowerCase().contains("thunder")) {
            throw new VehicleForbiddenException("Usage of selected vehicle is forbidden");
        }

        if (phenomenon.toLowerCase().contains("snow") || phenomenon.toLowerCase().contains("sleet")) {
            return BigDecimal.ONE;
        } else if (phenomenon.toLowerCase().contains("rain") || phenomenon.toLowerCase().contains("shower")) {
            return new BigDecimal("0.5");
        }

        return BigDecimal.ZERO;
    }

    private WeatherData fetchWeatherData(String city) throws NoWeatherFoundException {
        Optional<WeatherData> weatherDataOptional = Optional.ofNullable(weatherDataService.
                getLatestWeatherData(wmoCodes.get(city)));

        if (weatherDataOptional.isEmpty()) {
            throw new NoWeatherFoundException("No weather for " + city + " found in database!");
        }

        return weatherDataOptional.get();
    }

}
