package com.example.hortlink.data.dto;

public class CoordenadasDTO {
    private double lat;
    private double lng;

    public CoordenadasDTO(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() { return lat; }
    public double getLng() { return lng; }
}
