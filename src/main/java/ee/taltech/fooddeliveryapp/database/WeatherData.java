package ee.taltech.fooddeliveryapp.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Holds data about a weather observation made at a weather station.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "WEATHER_DATA")
public class WeatherData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stationName;

    private Integer wmoCode;
    private Double airTemperature;
    private Double windSpeed;
    private String weatherPhenomenon;
    private Long timeStamp;

    /**
     * Constructs a WeatherData object with all fields but the ID set.
     * @param stationName Station at which observation was taken
     * @param wmoCode WMO code of the station
     * @param airTemperature Air temperature of observation
     * @param windSpeed Wind speed of observation
     * @param weatherPhenomenon Any weather phenomena present
     * @param timeStamp UNIX from the time the observation was taken
     */
    public WeatherData(String stationName, Integer wmoCode, Double airTemperature, Double windSpeed,
                       String weatherPhenomenon, Long timeStamp) {
        this.stationName = stationName;
        this.wmoCode = wmoCode;
        this.airTemperature = airTemperature;
        this.windSpeed = windSpeed;
        this.weatherPhenomenon = weatherPhenomenon;
        this.timeStamp = timeStamp;
    }
}
