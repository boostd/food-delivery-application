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
import javax.xml.xpath.XPathExpressionException;
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
    private final int tartuCode = WMOCodes.TARTU_TORAVERE;
    private final int tallinnCode = WMOCodes.TALLINN_HARKU;
    private final int parnuCode = WMOCodes.PARNU;
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
        updateWeather(doc);
    }

    /**
     * Gets an XML file as input. Then parses it and saves WeatherData into the database.
     * Meant for testing.
     * @param doc XML file to parse
     */
    public void updateWeather(Document doc) {
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

            // Extract the timestamp and sanitize it
            Long timeStamp = Optional.ofNullable(xpath.evaluate("/observations/@timestamp", doc))
                    .filter(not(String::isEmpty))
                    .map(Long::parseLong)
                    .orElse(0L);

            // Create and use an XPath expression on the document
            String expression = buildXPathExpression();
            NodeList stations = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

            // Extract data from each station
            for (int i = 0; i < stations.getLength(); i++) {
                Node station = stations.item(i);

                output.add(createNotEmptyWeatherData(timeStamp, station, xpath));
            }

            WeatherData[] result = new WeatherData[output.size()];
            output.toArray(result);
            return result;

        } catch (XPathExpressionException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Dynamically build an XPath expression for stations whose WMO codes we're interested in.
     * Gets the WMO codes from a constant in WMOCodes class.
     * @return XPath's expression that parses XML from ilmateenistus.ee
     */
    private String buildXPathExpression() {
        int[] codes = WMOCodes.WMO_CODES;
        StringBuilder expression = new StringBuilder("/observations/station[wmocode='");

        for (int i = 0; i < codes.length; i++) {
            expression.append(codes[i]);
            if (i < codes.length - 1) {
                expression.append("' or wmocode='");
            }
        }

        return expression.append("']").toString();
    }

    /**
     * Creates a WeatherData object and makes sure the fields are not empty.
     * @return WeatherData object from a parsd XML file.
     */
    private WeatherData createNotEmptyWeatherData(Long timeStamp, Node station, XPath xpath)
            throws XPathExpressionException {

        String name = Optional.ofNullable(xpath.evaluate("name", station))
                .filter(not(String::isEmpty))
                .orElse("NaN");
        Integer wmoCode = (Optional.ofNullable(xpath.evaluate("wmocode", station)))
                .filter(not(String::isEmpty))
                .map(Integer::parseInt)
                .orElse(0);
        String phenomenon = Optional.of(xpath.evaluate("phenomenon", station))
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

        return new WeatherData(name, wmoCode, airTemperature, windSpeed, phenomenon, timeStamp);
    }

}
