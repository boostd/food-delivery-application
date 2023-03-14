package ee.taltech.fooddeliveryapp.database;

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
     * Saves the WeatherData into the in memory H2 database
     * @param weatherData WeatherData instance to save
     */
    public void saveWeatherData(WeatherData weatherData) {
        weatherDataRepository.save(weatherData);
    }
}
