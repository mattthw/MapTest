package edu.gatech.mmccoy37.maptest.pojo;

import android.location.Location;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by matt on 8/15/16.
 */
public class StationItem {

    private int emptySlots;
    private int freeBikes;
    private Location loc;
    private String name;
    private String address;
    private String timestamp;
    private String ID;

    public int getEmptySlots() {
        return emptySlots;
    }

    public void setEmptySlots(int emptySlots) {
        this.emptySlots = emptySlots;
    }

    public int getFreeBikes() {
        return freeBikes;
    }

    public void setFreeBikes(int freeBikes) {
        this.freeBikes = freeBikes;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    @Override
    public String toString() {
        return "StationItem{" +
                "address='" + address + '\'' +
                ", emptySlots=" + emptySlots +
                ", freeBikes=" + freeBikes +
                ", loc=" + loc +
                ", name='" + name + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", ID='" + ID + '\'' +
                '}';
    }


}
