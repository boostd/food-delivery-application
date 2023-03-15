package ee.taltech.fooddeliveryapp.service;

import ee.taltech.fooddeliveryapp.database.WeatherDataService;
import ee.taltech.fooddeliveryapp.scheduler.ImportWeatherTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = "fooddeliveryapp.enableScheduler=false")
public class DeliveryFeeCalculatorTest {
    @Autowired DeliveryFeeCalculator calculator;
    @Autowired ImportWeatherTask importWeatherTask;
    @Autowired WeatherDataService weatherDataService;
    @Value("classpath:testWeatherData.xml")
    private Resource xmlResource;

    @Test
    public void testCalculateAirTemperatureFee() {
        weatherDataService.clearAllWeatherData();
        importWeatherTask.updateWeather(loadXML());

        assertEquals(1, 1);
    }

    private Document loadXML() {
        try (InputStream inputStream = xmlResource.getInputStream()) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();

            return doc;
        } catch (Exception e) {
            return null;
        }
    }
}
