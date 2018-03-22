package is.stokkur.weatherinsight;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

/*
*   Small weather app written as part of recruiting process for Stokkur
*   Author: Danilo Rodrigues <nanoo_metaleiro@yahoo.com>
*   Free clipart images obtained from: http://clipart-library.com/img/1687903.jpg
*   Free app logo generated with the tool: http://www.freelogoservices.com
* */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private WeatherContentManager wm;
    private ForecastData forecastData;
    public static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private void refreshUi() {
        String path = "@drawable/icon"+forecastData.current_weather.icon;
        int imageRes = getResources().getIdentifier(path, null, getPackageName());
        ImageView currentWeather = (ImageView) findViewById(R.id.imageView_current);
        currentWeather.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                imageRes, null));

        /*findViewById(R.); // text view

        Fill 4 upcomingdays for tomorrow, x y z // icone, temperatura. achar um jeito de achar o dia da semana
        truncar temperatura

        fill dropdown list with extended forecast
        forecastData.prolonged_forecast.*/
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.fab:
                wm.refresh();
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

        // Trigger initial refresh
        wm.refresh();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                wm.refresh();
                forecastData = wm.getCurrentForecast();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        refreshUi();
                    }
                });
            }
        }, 10*1000, 10*60*1000); // 10 seconds before first run then every 10 minutes
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
}
