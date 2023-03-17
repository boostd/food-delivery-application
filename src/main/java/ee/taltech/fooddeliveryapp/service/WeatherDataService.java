package ee.taltech.fooddeliveryapp.service;

import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.database.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class WeatherDataService {
    private final WeatherDataRepository weatherDataRepository;
    private final HashMap<Integer, WeatherData> latestWeatherData = new HashMap<>();

    @Autowired
    public WeatherDataService(WeatherDataRepository weatherDataRepository) {
        this.weatherDataRepository = weatherDataRepository;
    }

    /**
     * Returns the latest weather data for the selected city (by WMO code)
     * If the cached HashMap has data for the specified WMO code, then return data from there.
     * Otherwise, queries the database.
     *
     * @param wmoCode WMO code of the city to search
     * @return Latest weather data for the city
     */
    public WeatherData getLatestWeatherData(Integer wmoCode) {
        WeatherData output = latestWeatherData.getOrDefault(wmoCode, null);
        return output != null ? output : weatherDataRepository.findFirstByWmoCodeOrderByTimeStampDesc(wmoCode);
    }

    /**
     * Save all WeatherData objects from the list into the H2 database.
     * Additionally, save them to a HashMap for fast lookup.
     *
     * @param weatherDataList WeatherData list to save
     */
    public void saveAllWeatherData(List<WeatherData> weatherDataList) {
        weatherDataRepository.saveAll(weatherDataList);

        latestWeatherData.clear();

        for (WeatherData weatherData : weatherDataList) {
            Integer wmoCode = weatherData.getWmoCode();
            if (wmoCode != null) {
                latestWeatherData.put(wmoCode, weatherData);
            }
        }
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
