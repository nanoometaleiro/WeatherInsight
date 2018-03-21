package is.stokkur.weatherinsight;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import org.w3c.dom.Document;

import java.io.BufferedReader;
//import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Permission;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * Content manager for weather data.
 * API Key: c894360e8c135f581e5f2f8bf7831bc6
 */

/* Example of weather xml content sent from the service:

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

public class WeatherContentManager {

    private final String APP_ID = "c894360e8c135f581e5f2f8bf7831bc6";
    private final Context context;
    private double latitude;
    private double longitude;
    private LocationManager locationManager;
    private Location location;
    private WeatherData weatherData;

    private class WeatherAsyncTask extends AsyncTask<Void, Void, WeatherData> {
        @Override
        protected WeatherData doInBackground(Void... params) {
            WeatherData wd = new WeatherData();
            try {
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

                Log.d("DBG", response);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document dom = dBuilder.parse(response);

                //dom.getElementsByTagName("Location");
                wd.location = "Miami, FL";
                wd.temperature = "100F";
                wd.pressure = "100psi";
                wd.description = "Windy";
                wd.humidity = "83%";
                wd.wind = "33 m/s NW";

                URL url_forecast = new URL("http://api.openweathermap.org/data/2.5/forecast?appid="
                        + APP_ID + "&units=metric&mode=xml&lat=" + String.valueOf(latitude)
                        + "&lon=" + String.valueOf(longitude));
                HttpURLConnection conn2 = (HttpURLConnection) url_forecast.openConnection();
                conn2.setReadTimeout(12000);
                conn2.setConnectTimeout(15000);
                conn2.setRequestMethod("GET");

                BufferedReader br2 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb2 = new StringBuilder();
                String line_f;
                while ((line_f = br2.readLine()) != null) {
                    sb2.append(line_f + "\n");
                }
                br.close();
                String response_f = sb2.toString();

                Log.d("DBG", response_f);

                Document dom_f = dBuilder.parse(response_f);

                // F
                /*for (int i = 0; i < 5; i < 5)
                {
                    wd.location = "Miami, FL";
                    wd.temperature = "100F";
                    wd.pressure = "100psi";
                    wd.description = "Windy";
                    wd.humidity = "83%";
                    wd.wind = "33 m/s NW";
                }*/

            } catch (Exception e) {
                e.printStackTrace();
            }
            return wd;
        }

        @Override
        protected void onPostExecute(WeatherData weatherData) {
            Log.d("DBG", "Weather Data retrieved succesfully");
        }
    }

    public WeatherContentManager (Context ctx) {
        context = ctx;
        // Setting defaults to Rvk coordinates
        latitude = 64.13548;
        longitude = -21.89541;
        weatherData = new WeatherData();
        updateLocation();
    }

    public WeatherData getCurrentWeather() {
        return weatherData;
    }

    private void updateLocation()
    {
        try {
            locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                int permission = PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
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
