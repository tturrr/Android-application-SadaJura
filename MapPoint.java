package com.example.user.sadajura;

public class MapPoint {
    private String Name;
    private double latitude;
    private double longitude;

    public MapPoint(){
        super();
    }

    public MapPoint(String name, double latitude, double longitude) {
        this.Name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
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
