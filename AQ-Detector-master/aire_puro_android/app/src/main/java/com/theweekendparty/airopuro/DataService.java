package com.theweekendparty.airopuro;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theweekendparty.airopuro.model.CommonValueApplication;
import com.theweekendparty.airopuro.model.Data;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Narayan on 26-Aug-17.
 */
public class DataService extends Service  {
    byte[] readBuffer;
    Thread workerThread;
    InputStream mmInputStream;
    int readBufferPosition = 0;
    volatile boolean stopWork = false;
    TextView outputText;
    double latitude, longitude;
    DatabaseReference database;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance().getReference();
        getLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        mmInputStream = CommonValueApplication.getInputStream();
        outputText = (TextView) CommonValueApplication.getView();
        beginListenForData();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWork) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            doSomethingWithData(data);
                                            outputText.setText(data + " PPM");

                                           //if()>200){
                                              //  outputText.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                                            //}
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWork = true;
                    }
                }
            }
        });
        workerThread.start();
    }

    public Location getLocation() {
        Location location = null;
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            // getting GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (location == null) {
                    long MIN_TIME_BW_UPDATES = 5;
                    float MIN_DISTANCE_CHANGE_FOR_UPDATES = 20;
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat
                            .checkSelfPermission(this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return null;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                                @Override
                                public void onStatusChanged(String s, int i, Bundle bundle) {

                                }
                                @Override
                                public void onProviderEnabled(String s) {

                                }
                                @Override
                                public void onProviderDisabled(String s) {

                                }
                            });
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation (LocationManager.GPS_PROVIDER);
                        if (location != null) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void doSomethingWithData(String pollution) {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        Data cData = new Data(pollution, timeStamp, latitude, longitude);
//        System.out.println(cData.toString());
        String id = database.push().getKey();
        database.child("values").child(id).setValue(cData);
        String temp = pollution.replace("\\", "");
        String n = "";
        for (int i=0; i<temp.length(); i++) {
            String t = String.valueOf(temp.charAt(i));
            if(t.equals(".")) {
                break;
            }
            n += t;
        }
        System.out.println(n);
        if (Integer.parseInt(n) < 150)
        {
            outputText.setBackground(getDrawable(R.drawable.circle_green));
        }
        else if (Integer.parseInt(n) > 150 && Integer.parseInt(n)<200) {
            outputText.setBackground(getDrawable(R.drawable.circle_yellow));
        } else if (Integer.parseInt(n) > 200) {
            outputText.setBackground(getDrawable(R.drawable.circle_red));
        }
    }

}