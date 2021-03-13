package com.example.bezpiecznajazda;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.KeyEventDispatcher;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private int last_x, last_y, last_z,first_x, first_y, first_z;
    private String x,y,adres;
    private static final int SHAKE_THRESHOLD = 400;//600;
    private  Button start;
    private static AlertDialog alertDialog;
    private boolean isOn=true;

    FusedLocationProviderClient locationClient;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        locationClient = LocationServices.getFusedLocationProviderClient(this);
        start=(Button)findViewById(R.id.start);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 1);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);

        start.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                if(isOn){
                    start.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.czerwony));
                    start.setText(R.string.stop);
                    isOn=false;
                }
                else{
                    start.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.zielony));
                    start.setText(R.string.start);
                    isOn=true;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menupasek,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.opcje){
            Intent intent=new Intent(this,opcje.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void SMS(String nr, String wiadomosc) {
        if(Looper.myLooper()==null)
            Looper.prepare();
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(nr, null, wiadomosc, null, null);
        Toast.makeText(getApplicationContext(), "SMS sent successfully.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {

                }

                last_x = (int) x;
                last_y = (int) y;
                last_z = (int) z;
                if (isOn) {
                    first_x = last_x;
                    first_y = last_y;
                    first_z = last_z;

                }
                int f=first_x - last_x;
                int s=first_y - last_y;
                int l=first_z - last_z;

                if ((f != 0) || (s != 0) || (l != 0)) {
                    int roznica=pobierzSkale();                                                                                                              //get roznica
                    if(bezwzgledna(f)>roznica||bezwzgledna(s)>roznica||bezwzgledna(l)>roznica) {
                    senSensorManager.unregisterListener(this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    View dialogView = getLayoutInflater().inflate(R.layout.komunikat, null);
                    builder.setView(dialogView);
                    alertDialog = builder.create();
                    alertDialog.show();
                    Button anuluj = (Button) dialogView.findViewById(R.id.anuluj);
                    ProgressBar pb = (ProgressBar) dialogView.findViewById(R.id.pb);
                    pb.setMax(1000);

                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i <= 1000; i++) {
                                if (alertDialog.isShowing()) {
                                    pb.setProgress(i);
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (alertDialog.isShowing()) {
                                getLocation();

                                String tresc = "lokacja wypadku X: " + x + " Y: " + y + "\nAdres: " + adres;
                                SMS(Integer.toString(pobierzNr()), tresc);
                                View dialogView = getLayoutInflater().inflate(R.layout.activity_main, null);

                                runOnUiThread(() -> {
                                    alertDialog.hide();
                                });
                            }
                            start.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.zielony));
                            start.setText(R.string.start);
                            isOn = true;
                            senSensorManager.registerListener(MainActivity.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);             //zmienićboolean


                        }
                    };
                    Thread t = new Thread(r);
                    t.start();

                    anuluj.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View v) {
                            alertDialog.hide();

                        }
                    });
                }
            }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest lr = new LocationRequest();
            lr.setInterval(10000);
            lr.setFastestInterval(3000);
            lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.getFusedLocationProviderClient(MainActivity.this)
                    .requestLocationUpdates(lr, new LocationCallback() {

                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                    .removeLocationUpdates(this);
                            if (locationResult != null && locationResult.getLocations().size() > 0) {
                                int latestLocationIndex = locationResult.getLocations().size() - 1;
                                double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                                double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                                List<Address> addresses=new ArrayList<>();
                                try {
                                    Geocoder geocoder=new Geocoder(MainActivity.this, Locale.getDefault());
                                    addresses=geocoder.getFromLocation(latitude,longitude,1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                x=Double.toString(latitude);
                                y=Double.toString(longitude);
                                adres=addresses.get(0).getAddressLine(0);
                            }
                        }
                    }, Looper.getMainLooper());
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

    }
    private int bezwzgledna(int param){
        if(param>=0)
            return param;
        else
            return -param;
    }
    private int pobierzNr(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=prefs.edit();
        return prefs.getInt("nr",123456789);
    }
    private int pobierzSkale(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=prefs.edit();
        return prefs.getInt("skala",1);
    }
}




