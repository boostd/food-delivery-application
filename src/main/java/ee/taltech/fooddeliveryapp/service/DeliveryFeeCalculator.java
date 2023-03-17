package ee.taltech.fooddeliveryapp.service;

import ee.taltech.fooddeliveryapp.config.DeliveryDataConstants;
import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.exceptions.InvalidTimeStampException;
import ee.taltech.fooddeliveryapp.exceptions.NoWeatherFoundException;
import ee.taltech.fooddeliveryapp.exceptions.UnknownCityException;
import ee.taltech.fooddeliveryapp.exceptions.UnknownVehicleException;
import ee.taltech.fooddeliveryapp.exceptions.VehicleForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryFeeCalculator {
    private final WeatherDataService weatherDataService;

    @Autowired
    public DeliveryFeeCalculator(WeatherDataService weatherDataService) {
        this.weatherDataService = weatherDataService;
    }

    /**
     * Calculates the delivery fee based on the current weather, the city selected, and the vehicle selected.
     *
     * @param city City to base the calculations off
     * @param vehicleType Vehicle to base the calculations off
     * @return Calculated fee. Returned as a BigDecimal as numbers must be accurate with money.
     * @throws UnknownCityException Thrown when the city isn't Tallinn, Tartu, or Pärnu
     * @throws UnknownVehicleException Thrown when the vehicle isn't a car, a scooter, or a bike
     * @throws NoWeatherFoundException Thrown when can't find any entries in the database for weather in the city
     * @throws VehicleForbiddenException Thrown when it is forbidden to deliver food with selected vehicle
     * @throws InvalidTimeStampException WeatherData is not valid for the targeted time
     */
    public BigDecimal calculateFee(String city, String vehicleType, LocalDateTime timeStamp)
            throws UnknownCityException, UnknownVehicleException, VehicleForbiddenException,
            NoWeatherFoundException, InvalidTimeStampException {
        city = city.toLowerCase();
        vehicleType = vehicleType.toLowerCase();

        if (!DeliveryDataConstants.CITY_LIST.contains(city)) {
            throw new UnknownCityException("No such city found!");
        } else if (!DeliveryDataConstants.VEHICLE_TYPE_LIST.contains(vehicleType)) {
            throw new UnknownVehicleException("No such vehicle found!");
        }

        Long UNIXTimeStamp = parseTimeToLong(timeStamp);
        BigDecimal baseFee = calculateBaseFee(city, vehicleType);
        BigDecimal weatherFee = calculateWeatherFee(city, vehicleType, UNIXTimeStamp);

        return baseFee.add(weatherFee);
    }

    /**
     * Calculates the base fee from the selected city and selected vehicle type.
     * Gets monetary values from the DeliveryData class.
     *
     * @param city Selected city
     * @param vehicleType Selected vehicle type
     * @return Calculated base fee according to business rules.
     */
    private BigDecimal calculateBaseFee(String city, String vehicleType) {
        BigDecimal fee = new BigDecimal("2.0");
        return fee.add(DeliveryDataConstants.CITY_FEES.get(city)).add(DeliveryDataConstants.
                VEHICLE_FEES.get(vehicleType));
    }

    /**
     * Calculates the additional weather fee according to current weather conditions in the selected city.
     *
     * @param city Selected city
     * @param vehicleType Selected vehicle type
     * @return Additional weather fee according to current weather conditions.
     * @throws NoWeatherFoundException No weather for the current city was found in the database.
     * @throws VehicleForbiddenException According to business rules it is forbidden to use the selected vehicle
     * @throws InvalidTimeStampException WeatherData is not valid for the targeted time
     */
    private BigDecimal calculateWeatherFee(String city, String vehicleType, Long timeStamp)
            throws NoWeatherFoundException, InvalidTimeStampException, VehicleForbiddenException {
        WeatherData data;

        // If the timestamp is null, then fetch the latest weather data
        if (timeStamp != null) {
            data = fetchWeatherData(city, timeStamp);
        } else {
            data = fetchWeatherData(city);
        }

        Double airTemperature = data.getAirTemperature();
        Double windSpeed = data.getWindSpeed();
        String phenomenon = data.getWeatherPhenomenon();

        BigDecimal airTemperatureFee = calculateAirTemperatureFee(vehicleType, airTemperature);
        BigDecimal windSpeedFee = calculateWindSpeedFee(vehicleType, windSpeed);
        BigDecimal phenomenonFee = calculatePhenomenonFee(vehicleType, phenomenon);

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

    /**
     * Fetches weather data for the selected city for the specified timestamp from the valid range for that timestamp.
     * Example: time of 10:45:32 would yield the latest WeatherData from the range 10:15:00 to 11:15:00
     *
     * @param city the name of the city to fetch the weather data for
     * @param timeStamp the Unix timestamp to fetch the weather data for
     * @return WeatherData for the specified city and timestamp
     * @throws InvalidTimeStampException WeatherData is not valid for the targeted time
     */
    private WeatherData fetchWeatherData(String city, Long timeStamp) throws InvalidTimeStampException {
        long[] range = findClosestTimeStamps(timeStamp);
        Optional<List<WeatherData>> weatherDataOptional = Optional.ofNullable(weatherDataService
                .getWeatherDataByTimeStamp(DeliveryDataConstants.WMO_CODES.get(city), range[0], range[1]));

        if (weatherDataOptional.isEmpty()) {
            throw new InvalidTimeStampException("No weather for " + city + " for requested time found in database!");
        } else if (weatherDataOptional.get().isEmpty()) {
            throw new InvalidTimeStampException("No weather for " + city + " for requested time found in database!");
        }

        List<WeatherData> weatherDataList = weatherDataOptional.get();

        return weatherDataList.get(weatherDataList.size() - 1);
    }

    /**
     * Fetches the latest weather data for selected city.
     *
     * @param city City to fetch the weather for.
     * @return The latest WeatherData for selected city
     * @throws NoWeatherFoundException No weather for selected city was found in the database
     */
    private WeatherData fetchWeatherData(String city) throws NoWeatherFoundException {
        Optional<WeatherData> weatherDataOptional = Optional.ofNullable(weatherDataService
                .getLatestWeatherData(DeliveryDataConstants.WMO_CODES.get(city)));

        if (weatherDataOptional.isEmpty()) {
            throw new NoWeatherFoundException("No weather for " + city + " found in database!");
        }

        return weatherDataOptional.get();
    }

    /**
     * Parses the LocalDateTime to a UNIX timestamp formatted as a long. Returns null if LocalDateTime is null
     *
     * @param timeStamp LocalDateTime timestamp to convert
     * @return Parsed UNIX timestamp or null
     */
    private Long parseTimeToLong(LocalDateTime timeStamp) {
        long output;
        try {
            output = Timestamp.valueOf(timeStamp).getTime() / 1000;
        } catch (NullPointerException e) {
            return null;
        }

        return output;
    }

    /**
     * Finds the Unix timestamps for the closest previous and next HH:15:00 time to the given Unix timestamp.
     *
     * @param unixTimeStamp the Unix timestamp for which to find the closest HH:15:00 times
     * @return an array containing the Unix timestamps for the closest previous and next HH:15:00 times
     */
    private long[] findClosestTimeStamps(long unixTimeStamp) {
        LocalDateTime dateTime = Instant.ofEpochSecond(unixTimeStamp).atZone(ZoneId.systemDefault()).toLocalDateTime();

        // Find the previous HH:15:00
        LocalDateTime previousDateTime = dateTime.truncatedTo(ChronoUnit.HOURS).plusMinutes(15);
        if (previousDateTime.isAfter(dateTime)) {
            previousDateTime = previousDateTime.minusHours(1);
        }

        // Find the next HH:15:00
        LocalDateTime nextDateTime = previousDateTime.plusHours(1);

        // Convert LocalDateTime back to Unix timestamps
        long previousUnixTimestamp = previousDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
        long nextUnixTimestamp = nextDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();

        // Return an array containing the previous and next Unix timestamps
        return new long[]{previousUnixTimestamp, nextUnixTimestamp};
    }

}
