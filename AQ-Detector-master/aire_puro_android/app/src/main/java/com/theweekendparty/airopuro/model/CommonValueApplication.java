package com.theweekendparty.airopuro.model;

import android.app.Application;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;

/**
 * Created by Jagadesh on 26-Aug-17.
 */

public class CommonValueApplication extends Application {

    public static InputStream minputStream;
    public  static View view,indicator;

    @Override
    public void onCreate() {
        super.onCreate();
        if(!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
    }

    public static View getView() {
        return view;
    }

    public static void setView(View view) {
        CommonValueApplication.view = view;
    }

    public static void setInputStream(InputStream is)
    {
        minputStream=is;
    }

    public static InputStream getInputStream()
    {
        return minputStream;
    }

    public static View getIndicator() {
        return indicator;
    }

    public static void setIndicator(View indicator) {
        CommonValueApplication.indicator = indicator;
    }
}
