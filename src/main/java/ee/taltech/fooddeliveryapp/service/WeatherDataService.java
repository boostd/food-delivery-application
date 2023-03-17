package ee.taltech.fooddeliveryapp.service;

import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.database.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeatherDataService {
    private final WeatherDataRepository weatherDataRepository;

    @Autowired
    public WeatherDataService(WeatherDataRepository weatherDataRepository) {
        this.weatherDataRepository = weatherDataRepository;
    }

    /**
     * Returns the latest weather data for the selected city (by WMO code)
     *
     * @param wmoCode WMO code of the city to search
     * @return Latest weather data for the city
     */
    public WeatherData getLatestWeatherData(Integer wmoCode) {
        return weatherDataRepository.findFirstByWmoCodeOrderByTimeStampDesc(wmoCode);
    }

    /**
     * Save all WeatherData objects from the list into the in memory H2 database
     *
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
     * Fetches the weather data for a selected city (by WMO code).
     * Returns valid weather for the selected time range.
     *
     * @param wmoCode WMO code of the weather station
     * @param start Targeted UNIX time for the start of the range
     * @param end Targeted UNIX time for the end of the range
     * @return Closest WeatherData entry to the targeted time
     */
    public List<WeatherData> getWeatherDataByTimeStamp(Integer wmoCode, long start, long end) {
        return weatherDataRepository.findByWmoCodeAndTimeStampBetweenOrderByTimeStampDesc(wmoCode, start, end);
    }
}
