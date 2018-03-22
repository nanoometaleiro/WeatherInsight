package is.stokkur.weatherinsight;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/*
*   Small weather app written as part of recruiting process for Stokkur
*   Author: Danilo Rodrigues <nanoo_metaleiro@yahoo.com>
*   Free clipart image obtained from: http://clipart-library.com/img/1687903.jpg
*   Free app logo generated with the tool: http://www.freelogoservices.com
* */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnItemSelectedListener
{

    private WeatherContentManager wm;
    private ForecastData forecastData;
    public static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private void refreshUi() {
        // Update main icon and weather info
        String path = "@drawable/icon"+forecastData.current_weather.icon;
        int imageRes = getResources().getIdentifier(path, null, getPackageName());
        ImageView iv = (ImageView) findViewById(R.id.imageView_current);
        iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                imageRes, null));
        TextView textView = (TextView) findViewById(R.id.textView_current);
        if (forecastData.current_weather.location == null) {
            textView.setText("Loading...");
            return;
        } else {
            textView.setText(forecastData.current_weather.location + "\n"
                    + forecastData.current_weather.description + "\n"
                    + forecastData.current_weather.temperature + "\n"
                    + "(Min: " + forecastData.current_weather.temperature_min + "/"
                    + "Max: " + forecastData.current_weather.temperature_max + ")\n"
                    + "Pressure: " + forecastData.current_weather.pressure + "\n"
                    + "Humidity: " + forecastData.current_weather.humidity + "\n"
                    + "Wind: " + forecastData.current_weather.wind);
        }

        // Update 4 upcoming days brief
        int filled = 0;
        for (int x = 0; x < forecastData.prolonged_forecast.size(); x++) {
            if  (filled == 4) break; // we have only for views for upcoming days
            Pair<String,WeatherData> pair = forecastData.prolonged_forecast.get(x);
            // A reference insight of a given's day weather would be at the middle
            // of the day, so we use 12:00
            Date now = new Date();
            String today = now.toString();
            if (pair.first.substring(11,13).equals("12")
                    && !pair.first.substring(8,10).equals(today.substring(8,10))) {
                switch (filled) {
                    case 0:
                        path = "@drawable/icon"+pair.second.icon;
                        imageRes = getResources().getIdentifier(path, null, getPackageName());
                        iv = (ImageView) findViewById(R.id.imageView_dayOne);
                        iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                imageRes, null));
                        textView = (TextView) findViewById(R.id.textView_dayOne);
                        textView.setText("Tomorrow\n"+pair.second.temperature+"\n"
                                +pair.second.description);
                        break;
                    case 1:
                        path = "@drawable/icon"+pair.second.icon;
                        imageRes = getResources().getIdentifier(path, null, getPackageName());
                        iv = (ImageView) findViewById(R.id.imageView_dayTwo);
                        iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                imageRes, null));
                        textView = (TextView) findViewById(R.id.textView_dayTwo);
                        textView.setText(pair.first.substring(8,10)+"/"
                                +pair.first.substring(5,7)+"\n"
                                +pair.second.temperature+"\n"
                                +pair.second.description);
                        break;
                    case 2:
                        path = "@drawable/icon"+pair.second.icon;
                        imageRes = getResources().getIdentifier(path, null, getPackageName());
                        iv = (ImageView) findViewById(R.id.imageView_dayThree);
                        iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                imageRes, null));
                        textView = (TextView) findViewById(R.id.textView_dayThree);
                        textView.setText(pair.first.substring(8,10)+"/"
                                +pair.first.substring(5,7)+"\n"
                                +pair.second.temperature+"\n"
                                +pair.second.description);
                        break;
                    case 3:
                        path = "@drawable/icon"+pair.second.icon;
                        imageRes = getResources().getIdentifier(path, null, getPackageName());
                        iv = (ImageView) findViewById(R.id.imageView_dayFour);
                        iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                imageRes, null));
                        textView = (TextView) findViewById(R.id.textView_dayFour);
                        textView.setText(pair.first.substring(8,10)+"/"
                                +pair.first.substring(5,7)+"\n"
                                +pair.second.temperature+"\n"
                                +pair.second.description);
                        break;
                }
                filled++;
            }
        }

        int size = forecastData.prolonged_forecast.size() + 1;
        String[] periods = new String[forecastData.prolonged_forecast.size()+1];
        periods[0] = "Choose a period...";

        for (int y = 1; y < size; y++)
            periods[y] = forecastData.prolonged_forecast.get(y-1).first;

        Spinner spinner = (Spinner) findViewById(R.id.spinner_extended);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, periods);
        spinner.setAdapter(ad);
        spinner.setOnItemSelectedListener(this);
    }

    private void scheduleRefreshUi() {
        wm.refresh();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                forecastData = wm.getCurrentForecast();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        refreshUi();
                    }
                });
            }
        }, 10*1000); // 10 seconds
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.fab:
                scheduleRefreshUi();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        // Permission check/request
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        wm = new WeatherContentManager(getApplicationContext());
        forecastData = wm.getCurrentForecast();
        refreshUi();

        // Trigger initial refresh
        scheduleRefreshUi();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                wm.refresh();
                ForecastData fd = wm.getCurrentForecast();
                if (fd.current_weather.location == null) {
                    return;
                }
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        refreshUi();
                    }
                });
            }
        }, 30*1000, 10*60*1000); // 30 seconds before first run, in case first attempt didnt
                                 // go through then every 10 minutes
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    * Here we get an item selected from the dropdown list for the extended forecast and
    * fill the information in the last textbox down below in the screen.
    * */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String str = (String) adapterView.getItemAtPosition(i);
        TextView tv = (TextView) findViewById(R.id.textView_extendedContent);
        if (str.equals("Choose a period...")) {
            tv.setText("");
        }
        for (int idx = 0; idx < forecastData.prolonged_forecast.size(); idx++) {
            if (forecastData.prolonged_forecast.get(idx).first.equals(str)) {
                WeatherData wd = forecastData.prolonged_forecast.get(idx).second;
                tv.setText(wd.description + "\n"
                        + wd.temperature + "\n"
                        + "(Min: " + wd.temperature_min + "/"
                        + "Max: " + wd.temperature_max + ")\n"
                        + "Pressure: " + wd.pressure + "\n"
                        + "Humidity: " + wd.humidity + "\n"
                        + "Wind: " + wd.wind);
                break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
