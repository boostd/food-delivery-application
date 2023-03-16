package ee.taltech.fooddeliveryapp.service;

import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.database.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeatherDataService {
    @Autowired
    private WeatherDataRepository weatherDataRepository;

    /**
     * Returns the latest weather data for the selected city (by WMO code)
     * @param wmoCode WMO code of the city to search
     * @return Latest weather data for the city
     */
    public WeatherData getLatestWeatherData(Integer wmoCode) {
        return weatherDataRepository.findByWmoCodeOrderByTimeStampDesc(wmoCode);
    }

    /**
     * Save all WeatherData objects from the list into the in memory H2 database
     * @param weatherDataList WeatherData list to save
     */
    public void saveAllWeatherData(List<WeatherData> weatherDataList) {
        weatherDataRepository.saveAll(weatherDataList);
    }

    /**
     * Clears all entries of WeatherData.
     */
    public void clearAllWeatherData() {
        weatherDataRepository.deleteAll();
    }

    /**
     * Fetches the weather data for a selected city (by WMO code). Returns the closest possible valid weather
     * for the selected time. The returned WeatherData should be checked if it is valid, as if an observation from
     * the target time was not stored in the database then the response might be an entry too early or too late.
     *
     * @param wmoCode WMO code of the weather station
     * @param targetTimeStamp Targeted UNIX time for the WeatherData entry
     * @return Closest WeatherData entry to the targeted time
     */
    public WeatherData getWeatherDataByTimeStamp(Integer wmoCode, long targetTimeStamp) {
        return weatherDataRepository.findFirstByWmoCodeAndTimeStampLessThanEqualOrderByTimeStampDesc
                (wmoCode, targetTimeStamp);
    }
}
