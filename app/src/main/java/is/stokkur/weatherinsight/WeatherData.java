package is.stokkur.weatherinsight;

import java.util.ArrayList;
import java.util.Map;

/**
 * Weather Data.
 */

public class WeatherData {
    public String location;
    public String description;
    public String temperature;
    public String humidity;
    public String wind;
    public String pressure;

    public Map<String, WeatherData> prolonged;
}
