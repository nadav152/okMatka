package com.example.okmatka;

public class UserLocation {

    private double latitude;
    private double longitude;

    public UserLocation() {
    }

    public UserLocation(double latitude, double longitude,boolean showLocation) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
