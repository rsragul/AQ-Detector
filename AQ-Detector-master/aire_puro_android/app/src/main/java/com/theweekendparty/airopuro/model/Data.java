package com.theweekendparty.airopuro.model;

/**
 * Created by Jagadesh on 26-Aug-17.
 */

public class Data {
    public String pollution, time;
    double latitude, longitude;

    public Data(String pollution, String time, double latitude, double longitude) {
        this.pollution = pollution;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Data() {}

    @Override
    public String toString() {
        return "Data{" +
                "pollution='" + pollution+
                ", time='" + time + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
