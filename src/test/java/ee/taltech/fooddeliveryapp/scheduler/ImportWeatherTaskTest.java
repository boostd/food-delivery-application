package ee.taltech.fooddeliveryapp.scheduler;

import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.service.WeatherDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;

import static org.mockito.Mockito.*;

class ImportWeatherTaskTest {

    private WeatherDataService weatherDataService;
    private ImportWeatherTask importWeatherTask;

    /**
     * Sets up the test environment, mocking the WeatherDataService and creating a new ImportWeatherTask instance.
     */
    @BeforeEach
    void setUp() {
        weatherDataService = Mockito.mock(WeatherDataService.class);
        importWeatherTask = new ImportWeatherTask(weatherDataService);
    }

    /**
     * Tests the updateWeather method, ensuring it processes the provided XML document and calls
     * the saveAllWeatherData method of WeatherDataService with the correct arguments.
     */
    @Test
    void testUpdateWeather() throws Exception {
        // Read the XML file from src/test/resources/testWeatherData.xml
        Document xmlDocument = readXmlFromFile("testWeatherData.xml");

        // Call the method you want to test with the prepared Document
        importWeatherTask.updateWeather(xmlDocument);

        // Verify that the weatherDataService.saveAllWeatherData() method was called with the correct arguments
        WeatherData expectedWeatherData1 = new WeatherData("Tallinn-Harku", 26038, 6.0, 4.3, "Overcast", 1678818585L);
        WeatherData expectedWeatherData2 = new WeatherData("Tartu-Tõravere", 26242, 5.0, 4.0, "Light shower", 1678818585L);
        WeatherData expectedWeatherData3 = new WeatherData("Pärnu", 41803, 4.3, 9.4, "NaN", 1678818585L);

        verify(weatherDataService, times(1)).saveAllWeatherData(List.of(expectedWeatherData1, expectedWeatherData2, expectedWeatherData3));
    }

    /**
     * Reads an XML file from the given file path and returns it as a Document object.
     *
     * @param filePath The path to the XML file
     * @return A Document object containing the XML data
     * @throws Exception If there's an error reading or parsing the file
     */
    private Document readXmlFromFile(String filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }
        return builder.parse(inputStream);
    }
}
