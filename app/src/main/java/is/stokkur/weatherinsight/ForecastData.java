package is.stokkur.weatherinsight;

import android.util.Pair;
import java.util.ArrayList;

public class ForecastData {
    public WeatherData current_weather;
    public ArrayList<Pair<String,WeatherData>> prolonged_forecast;
}
