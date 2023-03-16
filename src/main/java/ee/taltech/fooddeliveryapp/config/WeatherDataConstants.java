package ee.taltech.fooddeliveryapp.config;

/**
 * Holds the constants needed to fetch weather data from an external service
 *
 * WMO codes for weather stations we're interested in. (Tallinn-Harku, Tartu-Tõravere, and Pärnu)
 * Link to the service that serves weather data
 */
public class WeatherDataConstants {
    public final static int TALLINN_HARKU = 26038;
    public final static int TARTU_TORAVERE = 26242;
    public final static int PARNU = 41803;
    public final static int[] WMO_CODES = {TALLINN_HARKU, TARTU_TORAVERE, PARNU};
    public final static String WEATHER_SERVICE = "https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php";
}
