package ee.taltech.fooddeliveryapp.scheduler;

import ee.taltech.fooddeliveryapp.database.WeatherData;
import ee.taltech.fooddeliveryapp.database.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class ImportWeatherTask {

    private Document lastXML;
    private final ArrayList<Integer> wmoCodeList = new ArrayList<>(Arrays.asList(26038, 26242, 41803));

    @Autowired
    WeatherDataRepository weatherDataRepository;

    /**
     * Gets an XML file from
     * <a href="https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php">the weather service.</a>
     * Then parses the file and writes the weather data from the Tallinn-Harku, Tartu-Tõravere and Pärnu stations
     * into the database.
     */
    public void updateWeather() {
        Document doc = loadXML();
        WeatherData[] data = parseXML(doc);

        for (WeatherData entry : data) {
            if (!wmoCodeList.contains(entry.getWmoCode())) {
                continue;
            }

            weatherDataRepository.save(entry);
        }

    }

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

    private WeatherData[] parseXML(Document doc) {
        try {
            ArrayList<WeatherData> output = new ArrayList<>();

            String name;
            int wmocode;
            String phenomenon;
            double airTemperature;
            double windSpeed;

            // Extract the timestamp
            Integer timestamp = Optional.of(Integer.parseInt(doc.getDocumentElement().getAttribute("timestamp")))
                    .orElse(0);

            // Extract data from each station
            NodeList stationList = doc.getElementsByTagName("station");
            for (int i = 0; i < stationList.getLength(); i++) {
                Element station = (Element) stationList.item(i);
                name = Optional.ofNullable(station.getElementsByTagName("name").item(0).getTextContent())
                        .orElse("NaN");
                wmocode = Optional.of(Integer.parseInt(station.getElementsByTagName("wmocode").item(0)
                        .getTextContent())).orElse(0);
                phenomenon = Optional.of(station.getElementsByTagName("phenomenon").item(0).getTextContent())
                        .orElse("NaN");
                airTemperature = Optional.of(Double.parseDouble(station.getElementsByTagName("airtemperature")
                        .item(0).getTextContent())).orElse(0.0);
                windSpeed = Optional.of(Double.parseDouble(station.getElementsByTagName("windspeed").item(0)
                        .getTextContent())).orElse(0.0);

                output.add(new WeatherData(name, wmocode, airTemperature, windSpeed, phenomenon, timestamp));
            }

            //type checking to suppress a null warning
            WeatherData[] result = new WeatherData[output.size()];
            output.toArray(result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
