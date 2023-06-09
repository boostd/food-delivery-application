package ee.taltech.fooddeliveryapp.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Holds constants of business rules used for calculating the delivery fee.
 */
public class DeliveryDataConstants {
    public static final HashMap<String, BigDecimal> CITY_FEES = new HashMap<>() {{
        put("tallinn", BigDecimal.ONE);
        put("tartu", new BigDecimal("0.5"));
        put("pärnu", BigDecimal.ZERO);
    }};
    public static final HashMap<String, BigDecimal> VEHICLE_FEES = new HashMap<>() {{
        put("car", BigDecimal.ONE);
        put("scooter", new BigDecimal("0.5"));
        put("bike", BigDecimal.ZERO);
    }};
    public static final HashMap<String, Integer> WMO_CODES = new HashMap<>() {{
        put("tallinn", WeatherDataConstants.TALLINN_HARKU);
        put("tartu", WeatherDataConstants.TARTU_TORAVERE);
        put("pärnu", WeatherDataConstants.PARNU);
    }};
    public static final ArrayList<String> CITY_LIST = new ArrayList<>(Arrays.asList("tallinn", "tartu", "pärnu"));
    public static final ArrayList<String> VEHICLE_TYPE_LIST = new ArrayList<>(Arrays.asList("car", "scooter", "bike"));
}
