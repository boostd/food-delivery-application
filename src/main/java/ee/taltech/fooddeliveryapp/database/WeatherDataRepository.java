package ee.taltech.fooddeliveryapp.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    /**
     * Creates and returns a new WeatherData object. Sets all respective variables.
     * @param stationName Weather station at which observation was taken
     * @param wmoCode WMO code of the station
     * @param airTemperature Observed air temperature
     * @param windSpeed Observed wind speed
     * @param weatherPhenomenon Any observed weather phenomena
     * @param timeStamp UNIX time at which measurements were taken
     * @return A new WeatherData object with all corresponding values set
     */
    /*public WeatherData createNewWeatherData(String stationName, String wmoCode,
                                            Double airTemperature, Double windSpeed,
                                            String weatherPhenomenon, LocalDateTime timeStamp);*/

}
