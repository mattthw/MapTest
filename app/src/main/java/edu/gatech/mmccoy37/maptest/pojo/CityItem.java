package edu.gatech.mmccoy37.maptest.pojo;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by matt on 8/15/16.
 */
public class CityItem {

    private String name = "CITY_NAME";
    private String id = "";
    private Location loc;
    private String href;


    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public void setID(String s) {
        this.id = s;
    }
    public String getID() {
        return this.id;
    }
    public void setLoc(Location l) {
        this.loc = l;
    }
    public Location getLoc() {
        return this.loc;
    }
}
