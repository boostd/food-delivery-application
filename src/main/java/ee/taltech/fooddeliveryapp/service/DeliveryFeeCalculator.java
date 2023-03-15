package ee.taltech.fooddeliveryapp.service;

import ee.taltech.fooddeliveryapp.common.DeliveryData;
import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.database.WeatherDataService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;

public class DeliveryFeeCalculator {
    @Autowired
    WeatherDataService weatherDataService;

    /**
     * Calculates the delivery fee based on the current weather, the city selected, and the vehicle selected.
     * @param city City to base the calculations off
     * @param vehicleType Vehicle to base the calculations off
     * @return Calculated fee. Returned as a BigDecimal as numbers must be accurate with money.
     * @throws UnknownCityException Thrown when the city isn't Tallinn, Tartu, or Pärnu
     * @throws UnknownVehicleException Thrown when the vehicle isn't a car, a scooter, or a bike
     * @throws NoWeatherFoundException Thrown when can't find any entries in the database for weather in the city
     * @throws VehicleForbiddenException Thrown when it is forbidden to deliver food with selected vehicle
     */
    public BigDecimal calculateFee(String city, String vehicleType)
            throws UnknownCityException, UnknownVehicleException, NoWeatherFoundException, VehicleForbiddenException {
        city = city.toLowerCase();
        vehicleType = vehicleType.toLowerCase();

        if (!DeliveryData.CITY_LIST.contains(city)) {
            throw new UnknownCityException("No such city found!");
        } else if (!DeliveryData.VEHICLE_TYPE_LIST.contains(vehicleType)) {
            throw new UnknownVehicleException("No such vehicle found!");
        }

        BigDecimal baseFee = calculateBaseFee(city, vehicleType);
        BigDecimal weatherFee = calculateWeatherFee(city, vehicleType);

        return baseFee.add(weatherFee);
    }

    /**
     * Calculates the base fee from the selected city and selected vehicle type. Gets monetary values from the
     * DeliveryData class.
     * @param city Selected city
     * @param vehicleType Selected vehicle type
     * @return Calculated base fee according to business rules.
     */
    private BigDecimal calculateBaseFee(String city, String vehicleType) {
        BigDecimal fee = new BigDecimal("2.0");
        return fee.add(DeliveryData.CITY_FEES.get(city)).add(DeliveryData.
                VEHICLE_FEES.get(vehicleType));
    }

    /**
     * Calculates the additional weather fee according to current weather conditions in the selected city.
     * @param city Selected city
     * @param vehicleType Selected vehicle type
     * @return Additional weather fee according to current weather conditions.
     * @throws NoWeatherFoundException No weather for the current city was found in the database.
     * @throws VehicleForbiddenException According to business rules it is forbidden to use the selected vehicle
     */
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
            return BigDecimal.ONE;
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
                getLatestWeatherData(DeliveryData.WMO_CODES.get(city)));

        if (weatherDataOptional.isEmpty()) {
            throw new NoWeatherFoundException("No weather for " + city + " found in database!");
        }

        return weatherDataOptional.get();
    }

}
