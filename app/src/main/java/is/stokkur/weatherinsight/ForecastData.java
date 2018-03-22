package is.stokkur.weatherinsight;

import android.util.Pair;
import java.io.Serializable;
import java.util.ArrayList;

public class ForecastData implements Serializable {
    public WeatherData current_weather;
    public ArrayList<Pair<String,WeatherData>> prolonged_forecast;
}
