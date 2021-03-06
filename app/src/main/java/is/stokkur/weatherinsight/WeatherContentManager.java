package is.stokkur.weatherinsight;

import android.Manifest;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.util.Pair;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Content manager for weather data.
 * API Key: c894360e8c135f581e5f2f8bf7831bc6
 * This classe is totally decoupled from UI
 * elements and focus on manipulating the
 * data obtained from the weather service
 */

public class WeatherContentManager {

    private final String APP_ID = "c894360e8c135f581e5f2f8bf7831bc6";
    private final Context context;
    private double latitude;
    private double longitude;
    private LocationManager locationManager;
    private Location location;
    private ForecastData forecastData;

    private class WeatherAsyncTask extends AsyncTask<Void, Void, ForecastData> {
        @Override
        protected ForecastData doInBackground(Void... params) {
            try {
                /*
                This is an example of a weather message response, which is different than
                the extended forecast message.

                <current>
                    <city id="2643743" name="London">
                        <coord lon="-0.13" lat="51.51"/>
                        <country>GB</country>
                        <sun rise="2018-03-19T06:04:50" set="2018-03-19T18:12:27"/>
                    </city>
                    <temperature value="272.75" min="272.15" max="273.15" unit="kelvin"/>
                    <humidity value="82" unit="%"/>
                    <pressure value="1014" unit="hPa"/>
                    <wind>
                        <speed value="8.2" name="Fresh Breeze"/>
                        <gusts value="13.4"/>
                        <direction value="60" code="ENE" name="East-northeast"/>
                    </wind>
                    <clouds value="75" name="broken clouds"/>
                    <visibility value="10000"/>
                    <precipitation mode="no"/>
                    <weather number="803" value="broken clouds" icon="04d"/>
                    <lastupdate value="2018-03-19T09:50:00"/>
                </current>
                */

                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?appid="
                        + APP_ID + "&units=metric&mode=xml&lat=" + String.valueOf(latitude)
                        + "&lon=" + String.valueOf(longitude));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(12000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                String response = sb.toString();

                //Log.d("DBG", response);
                parseCurrentWeatherResponse(response);

                /*
                * This is an example of the extended forecast message response from the service
                * Note that it is truncated and show only one <time> element out of the usual 40
                *
                * <forecast>
                *     <time from="2018-03-21T12:00:00" to="2018-03-21T15:00:00">
                *         <symbol number="800" name="clear sky" var="01d"/>
                *         <precipitation/>
                *         <windDirection deg="270.5" code="W" name="West"/>
                *         <windSpeed mps="4.06" name="Gentle Breeze"/>
                *         <temperature unit="kelvin" value="282.42" min="280.89" max="282.42"/>
                *         <pressure unit="hPa" value="1035.82"/><humidity value="75" unit="%"/>
                *         <clouds value="clear sky" all="0" unit="%"/>
                *     </time>
                *     ...
                * </forecast>
                **/
                URL url_forecast = new URL("http://api.openweathermap.org/data/2.5/forecast?appid="
                        + APP_ID + "&units=metric&mode=xml&lat=" + String.valueOf(latitude)
                        + "&lon=" + String.valueOf(longitude));
                HttpURLConnection conn2 = (HttpURLConnection) url_forecast.openConnection();
                conn2.setReadTimeout(12000);
                conn2.setConnectTimeout(15000);
                conn2.setRequestMethod("GET");

                BufferedReader br2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                StringBuilder sb2 = new StringBuilder();
                String line_f;
                while ((line_f = br2.readLine()) != null) {
                    sb2.append(line_f + "\n");
                }
                br2.close();
                response = sb2.toString();

                //Log.d("DBG", response);
                parseExtendedForecastResponse(response);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return forecastData;
        }

        @Override
        protected void onPostExecute(ForecastData forecastData) {
            Log.d("DBG", "Weather Data retrieved successfully");
        }
    }

    private void parseExtendedForecastResponse(String response) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(response));
            Document dom = dBuilder.parse(is);

            NodeList ndl = dom.getElementsByTagName("forecast");
            Element nd = (Element) ndl.item(0);

            forecastData.prolonged_forecast.clear();
            Node currentChild = nd.getFirstChild();
            String period;
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) currentChild;
                    WeatherData wdx = new WeatherData();
                    period = e.getAttribute("from");
                    period = period.replace('T', ' ');

                    NodeList ndx = e.getElementsByTagName("temperature");
                    Element nde = (Element) ndx.item(0);
                    wdx.temperature = nde.getAttribute("value");
                    if (wdx.temperature.contains(".")) {
                        wdx.temperature = wdx.temperature.substring(
                                0, wdx.temperature.indexOf("."));
                    }
                    wdx.temperature += " °C";
                    wdx.temperature_min = nde.getAttribute("min") + " °C";
                    wdx.temperature_max = nde.getAttribute("max") + " °C";

                    ndx = e.getElementsByTagName("humidity");
                    nde = (Element) ndx.item(0);
                    wdx.humidity = nde.getAttribute("value") + nde.getAttribute("unit");

                    ndx = e.getElementsByTagName("symbol");
                    nde = (Element) ndx.item(0);
                    wdx.icon = nde.getAttribute("var");
                    wdx.description = nde.getAttribute("name");

                    ndx = e.getElementsByTagName("windSpeed");
                    nde = (Element) ndx.item(0);
                    wdx.wind = nde.getAttribute("mps") + " m/s ";

                    ndx = e.getElementsByTagName("windDirection");
                    nde = (Element) ndx.item(0);
                    wdx.wind += nde.getAttribute("code");

                    ndx = e.getElementsByTagName("pressure");
                    nde = (Element) ndx.item(0);
                    wdx.pressure = nde.getAttribute("value") + " " + nd.getAttribute("unit");

                    wdx.location = forecastData.current_weather.location;

                    forecastData.prolonged_forecast.add(
                            new Pair<>(period, wdx));
                }
                currentChild = currentChild.getNextSibling();
            }
            if (forecastData.prolonged_forecast.size() == 40) { // Checks if the message is complete
                saveCacheData(response, "extended.dat");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseCurrentWeatherResponse(String response) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(response));
            Document dom = dBuilder.parse(is);

            NodeList ndl = dom.getElementsByTagName("temperature");
            Element nd = (Element) ndl.item(0);
            forecastData.current_weather.temperature = nd.getAttribute("value");
            if (forecastData.current_weather.temperature.contains(".")) {
                forecastData.current_weather.temperature =
                        forecastData.current_weather.temperature.substring(
                                0, forecastData.current_weather.temperature.indexOf("."));
            }
            forecastData.current_weather.temperature += " °C";
            forecastData.current_weather.temperature_min = nd.getAttribute("min") + " °C";
            forecastData.current_weather.temperature_max = nd.getAttribute("max") + " °C";

            ndl = dom.getElementsByTagName("city");
            nd = (Element) ndl.item(0);
            forecastData.current_weather.location = nd.getAttribute("name");

            ndl = dom.getElementsByTagName("weather");
            nd = (Element) ndl.item(0);
            forecastData.current_weather.description = nd.getAttribute("value");
            forecastData.current_weather.icon = nd.getAttribute("icon");

            ndl = dom.getElementsByTagName("speed");
            nd = (Element) ndl.item(0);
            forecastData.current_weather.wind = nd.getAttribute("value") + " m/s ";

            ndl = dom.getElementsByTagName("direction");
            nd = (Element) ndl.item(0);
            forecastData.current_weather.wind += nd.getAttribute("code");

            ndl = dom.getElementsByTagName("humidity");
            nd = (Element) ndl.item(0);
            forecastData.current_weather.humidity = nd.getAttribute("value")
                    + nd.getAttribute("unit");

            ndl = dom.getElementsByTagName("pressure");
            nd = (Element) ndl.item(0);
            forecastData.current_weather.pressure = nd.getAttribute("value")
                    + " " + nd.getAttribute("unit");
            if (forecastData.current_weather.location != null) { // sanity check
                saveCacheData(response, "weather.dat");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WeatherContentManager (Context ctx) {
        context = ctx;
        // Setting default coordinates to Reykjavik :)
        latitude = 64.13548;
        longitude = -21.89541;
        forecastData = new ForecastData();
        forecastData.current_weather = new WeatherData();
        forecastData.current_weather.icon = "01d"; // Default icon code in case data is not yet
                                                   // available

        forecastData.prolonged_forecast = new ArrayList<Pair<String,WeatherData>>();
        updateLocation();
        loadCachedData();
    }

    private void saveCacheData(String str, String fileName) {
        try {
            File file = new File(context.getCacheDir(), fileName);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(str);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadCachedData() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    context.getCacheDir()+"/weather.dat"));
            StringBuilder sb = new StringBuilder();
            String line_f;
            while ((line_f = br.readLine()) != null) {
                sb.append(line_f + "\n");
            }
            br.close();
            String response = sb.toString();
            parseCurrentWeatherResponse(response);

            br = new BufferedReader(new FileReader(context.getCacheDir()+"/extended.dat"));
            sb = new StringBuilder();
            while ((line_f = br.readLine()) != null) {
                sb.append(line_f + "\n");
            }
            br.close();
            response = sb.toString();
            parseExtendedForecastResponse(response);

        } catch (FileNotFoundException f) {
            Log.d("DBG","No cached data found");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ForecastData getCurrentForecast() {
        return forecastData;
    }

    /* This method updates the location used in the app. Note that when using the emulator
    * it will rely on last known location, so use a google maps first, just to be sure the
    * emulator has some known location. */
    private void updateLocation()
    {
        try {
            locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                int permission = PermissionChecker.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permission == PermissionChecker.PERMISSION_GRANTED) {
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    criteria.setCostAllowed(false);

                    String provider = locationManager.getBestProvider(criteria, false);
                    location = locationManager.getLastKnownLocation(provider);
                }
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
        updateLocation();
        new WeatherAsyncTask().execute();
    }
}
