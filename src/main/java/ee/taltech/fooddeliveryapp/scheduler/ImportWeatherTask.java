package ee.taltech.fooddeliveryapp.scheduler;

import ee.taltech.fooddeliveryapp.common.WMOCodes;
import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.database.WeatherDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static java.util.function.Predicate.not;

public class ImportWeatherTask {

    /**
     * WMO codes of the Tallinn-Harku, Tartu-Tõravere, and Pärnu weather stations respectively.
     */
    private final ArrayList<Integer> wmoCodeList = new ArrayList<>(Arrays.asList(WMOCodes.TALLINN_HARKU,
            WMOCodes.TARTU_TORAVERE, WMOCodes.PARNU));
    private Document lastXML;

    @Autowired
    WeatherDataService weatherDataService;

    /**
     * Gets an XML file from
     * <a href="https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php">the weather service.</a>
     * Then parses the file and writes the weather data from the Tallinn-Harku, Tartu-Tõravere and Pärnu stations
     * into the database.
     */
    public void updateWeather() {
        Document doc = loadXML();
        WeatherData[] data = parseXML(doc);

        weatherDataService.saveAllWeatherData(Arrays.asList(data));
    }

    /**
     * Gets the XML file of weather data from ilmateenistus.ee and returns it as a Document.
     * @return Current weather data as a Document.
     */
    private Document loadXML() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            URL url = new URL("https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php");
            InputStream stream = url.openStream();
            Document doc = builder.parse(stream);
            doc.getDocumentElement().normalize();

            lastXML = doc;
            return doc;
        } catch (Exception e) {
            if (lastXML == null) {
                throw new RuntimeException();
            }

            return lastXML;
        }
    }

    /**
     * Loops over the Document and finds the required stations. Checks if values are empty in which case assigns
     * default values. Instantiates WeatherData objects and returns them as an array.
     * @param doc XML Document containing the current weather data.
     * @return WeatherData array from Tallinn-Harku, Tartu-Tõravere, and Pärnu weather stations.
     */
    private WeatherData[] parseXML(Document doc) {
        try {
            ArrayList<WeatherData> output = new ArrayList<>();
            XPath xpath = XPathFactory.newInstance().newXPath();

            // Extract the timestamp
            Integer timeStamp = Integer.parseInt((Optional.of(doc.getDocumentElement().getAttribute("timestamp")))
                    .orElse("0"));

            // Extract data from each station
            NodeList stations = (NodeList) xpath.evaluate("//station", doc, XPathConstants.NODESET);
            for (int i = 0; i < stations.getLength(); i++) {
                Node station = stations.item(i);
                Integer wmoCode = (Optional.ofNullable(xpath.evaluate("wmocode", station)))
                        .filter(not(String::isEmpty))
                        .map(Integer::parseInt)
                        .orElse(0);

                //Only proceed if current station is one of three were interested in
                if (!wmoCodeList.contains(wmoCode)) {
                    continue;
                }

                String name = Optional.ofNullable(xpath.evaluate("name", station))
                        .filter(not(String::isEmpty))
                        .orElse("NaN");
                String phenomenon = (Optional.of(xpath.evaluate("phenomenon", station)))
                        .filter(not(String::isEmpty))
                        .orElse("NaN");
                Double airTemperature = Optional.of(xpath.evaluate("airtemperature", station))
                        .filter(not(String::isEmpty))
                        .map(Double::parseDouble)
                        .orElse(0.0);
                Double windSpeed = Optional.of(xpath.evaluate("windspeed", station))
                        .filter(not(String::isEmpty))
                        .map(Double::parseDouble)
                        .orElse(0.0);

                output.add(new WeatherData(name, wmoCode, airTemperature, windSpeed, phenomenon, timeStamp));
            }

            WeatherData[] result = new WeatherData[output.size()];
            output.toArray(result);
            return result;

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
