package com.theweekendparty.airopuro;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.theweekendparty.airopuro.model.CommonValueApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    TextView textview3, textView4, textView5;
    TextView outputLable;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputLable = findViewById(R.id.label);
        textview3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);
        CommonValueApplication.setView(outputLable);
        findBT();
        try {
            openBT();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Typeface roboto= Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        textview3.setTypeface(roboto);
        textView4.setTypeface(roboto);
        textView5.setTypeface(roboto);
        
        outputLable.setTypeface(roboto);

    }

    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            outputLable.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) {
                    mmDevice = device;
                    break;
                }
            }
        }
    }

    void openBT() throws IOException {

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        //beginListenForData();
        Intent serviceIntent = new Intent(this, DataService.class);
        CommonValueApplication.setInputStream(mmInputStream);
        startService(serviceIntent);
        Toast.makeText(MainActivity.this, "Connected To Puro", Toast.LENGTH_SHORT).show();
    }

}