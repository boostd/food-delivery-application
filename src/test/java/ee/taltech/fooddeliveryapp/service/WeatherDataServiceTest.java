package ee.taltech.fooddeliveryapp.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.database.WeatherDataRepository;

public class WeatherDataServiceTest {

    private WeatherDataRepository weatherDataRepository;
    private WeatherDataService weatherDataService;

    @BeforeEach
    public void setUp() {
        weatherDataRepository = mock(WeatherDataRepository.class);
        weatherDataService = new WeatherDataService(weatherDataRepository);
    }

    /**
     * Tests the getLatestWeatherData method by checking if the correct weather data is returned for the given WMO code.
     */
    @Test
    public void testGetLatestWeatherData() {
        WeatherData expected = new WeatherData();
        expected.setWmoCode(12345);
        expected.setTimeStamp(1647589200L);

        when(weatherDataRepository.findFirstByWmoCodeOrderByTimeStampDesc(12345)).thenReturn(expected);

        WeatherData actual = weatherDataService.getLatestWeatherData(12345);

        assertEquals(expected, actual);
        verify(weatherDataRepository, times(1)).findFirstByWmoCodeOrderByTimeStampDesc(12345);
    }

    /**
     * Tests the saveAllWeatherData method by verifying if the repository's saveAll method is called with the correct input.
     */
    @Test
    public void testSaveAllWeatherData() {
        List<WeatherData> weatherDataList = new ArrayList<>();
        weatherDataList.add(new WeatherData());
        weatherDataList.add(new WeatherData());
        weatherDataList.add(new WeatherData());

        weatherDataService.saveAllWeatherData(weatherDataList);

        verify(weatherDataRepository, times(1)).saveAll(weatherDataList);
    }

    /**
     * Tests the clearAllWeatherData method by verifying if the repository's deleteAll method is called.
     */
    @Test
    public void testClearAllWeatherData() {
        weatherDataService.clearAllWeatherData();

        verify(weatherDataRepository, times(1)).deleteAll();
    }

    /**
     * Tests the getWeatherDataByTimeStamp method by checking if the correct weather data list
     * is returned for the given WMO code and time range.
     */
    @Test
    public void testGetWeatherDataByTimeStamp() {
        List<WeatherData> expected = new ArrayList<>();
        expected.add(new WeatherData());
        expected.add(new WeatherData());
        expected.add(new WeatherData());

        when(weatherDataRepository.findByWmoCodeAndTimeStampBetweenOrderByTimeStampDesc
                (12345, 1647589200L, 1647603600L)).thenReturn(expected);

        List<WeatherData> actual = weatherDataService
                .getWeatherDataByTimeStamp(12345, 1647589200L, 1647603600L);

        assertEquals(expected, actual);
        verify(weatherDataRepository, times(1))
                .findByWmoCodeAndTimeStampBetweenOrderByTimeStampDesc(12345, 1647589200L, 1647603600L);
    }
}
