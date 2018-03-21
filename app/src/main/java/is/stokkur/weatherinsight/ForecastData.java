package is.stokkur.weatherinsight;

import android.util.Pair;
import java.io.Serializable;

public class ForecastData implements Serializable {
    public WeatherData current_weather;
    public Pair<String,WeatherData> prolonged_forecast;
}
