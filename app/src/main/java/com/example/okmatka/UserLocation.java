package com.example.okmatka;

public class UserLocation {

    private double latitude;
    private double longitude;
    private boolean sendMyLocation;

    public UserLocation() {
    }

    public UserLocation(double latitude, double longitude,boolean showLocation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.sendMyLocation = showLocation;
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

    public boolean isSendHisLocation() {
        return sendMyLocation;
    }

    public void setSendMyLocation(boolean sendMyLocation) {
        this.sendMyLocation = sendMyLocation;
    }
}
