package dev.hersteller.weatherupdate;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import model.Weather;
import model.WeatherModel;
import rest.ApiService;
import rest.RetroClient;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_CODE = 1;
    private static final String API_KEY = "53f9d8e4213222cf517d86dc406d67fc";

    private int latitude = 0;
    private int longitude =  0;
    private LocationManager locationManager;
    Location location = null;
    private String provider;

    private Button refreshButton;

    TextView temperature, seaLevel, humidity, sunrise, sunset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperature =  findViewById(R.id.temp);
        seaLevel =  findViewById(R.id.sea_level);
        humidity =  findViewById(R.id.humidity);
        sunrise = findViewById(R.id.sunrise);
        sunset = findViewById(R.id.sunset);

        refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationManager();
            }
        });

        try {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission has already been granted
                // Get the location manager
                getLocationManager();
            } else {
                requestLocationPermission();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Main Activity", "onCreate: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
                    getLocationManager();


                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // case
        }
    }

    private void requestLocationPermission() {
        try {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location permission needed")
                        .setMessage("Device location needs to be on.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSION_CODE);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create().show();

            } else {
                // No explanation needed; request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Main Activity", "requestLocationPermission: " + e.getMessage());
        }

    }

    private void getLocationManager() {
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
            makeHttpCall();
        } else {
            Toast.makeText(MainActivity.this,
                    "Please check if your internet settings are set correctly.", Toast.LENGTH_LONG).show();
        }
    }

    public void makeHttpCall(){

//        String lat = String.valueOf(location.getLatitude());
//        String lon = String.valueOf(location.getLongitude());
        // show a progress dialog while data is being fetched
        final ProgressDialog dialog;
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle("Loading");
        dialog.setMessage("Please wait");
        dialog.show();
        //Create an object of our api interface
        ApiService api = RetroClient.getApiService();
        // Calling JSON
        retrofit2.Call<WeatherModel> call = api.getWeatherUpdate(latitude, longitude, API_KEY);
        //Enqueue Callback will be call when get response
        call.enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(retrofit2.Call<WeatherModel> call, Response<WeatherModel> response) {
                // Dismiss dialog
                dialog.dismiss();
                Log.d("Main Activity", response.toString());
                if(response.isSuccessful()){
                    temperature.setText(String.valueOf((response.body().getMain().getTemp()) - 273.15));
                    humidity.setText(String.valueOf(response.body().getMain().getHumidity()));
                    sunrise.setText(String.valueOf(response.body().getSys().getSunrise()));
                    sunset.setText(String.valueOf(response.body().getSys().getSunset()));
                    seaLevel.setText(String.valueOf(response.body().getMain().getSeaLevel()));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<WeatherModel> call, Throwable t) {
                // Dismiss dialog
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Weather update not successful", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (location != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = (int) (location.getLatitude());
        longitude = (int) (location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(this, provider + " enabled",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, provider + " disabled",
                Toast.LENGTH_SHORT).show();
    }
}
