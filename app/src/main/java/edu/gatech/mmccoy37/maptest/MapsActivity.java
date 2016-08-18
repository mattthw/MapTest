package edu.gatech.mmccoy37.maptest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.location.LocationListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import edu.gatech.mmccoy37.maptest.pojo.CityItem;
import edu.gatech.mmccoy37.maptest.pojo.StationItem;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    public static GoogleMap mMap;
    static Marker selectedStationMarker;
    private SupportMapFragment mapFragment;
    private Location mLocation;
    private static Activity mActivity;
    private static HashMap<Location, StationItem> stationMap;
    private static Stack<Marker> STATIONS_ON_MAP;
    static final int STORAGE_PERMISSION_REQUEST = 1;
    static final int LOCATION_PERMISSION_REQUEST = 2;
    static final float DEFAULT_ZOOM_CLOSE = 13.0f;
    static final float DEFAULT_ZOOM_FAR = 6.0f;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.mActivity = this;
        STATIONS_ON_MAP = new Stack<>();
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //get location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "REQUIRES LOCATION PERMISSION", Toast.LENGTH_LONG);
            return;
        }
        lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
        this.mLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    public static HashMap<Location, StationItem> getStationMap() {
        return stationMap;
    }

    public static void setStationMap(HashMap<Location, StationItem> stationMap) {
        MapsActivity.stationMap = stationMap;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 0);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTag() instanceof CityItem) {
                    Log.d("API_TAG", "clicked marker: " + marker.getTitle());
                    AsyncFetchStations fetch = new AsyncFetchStations();
                    fetch.execute(((CityItem)marker.getTag()).getHref());
                    LatLng pos = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                    mMap.getUiSettings().setMapToolbarEnabled(false);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, DEFAULT_ZOOM_CLOSE));
                    mMap.setMinZoomPreference(DEFAULT_ZOOM_CLOSE);
                    return true;
                } else if (marker.getTag() instanceof StationItem){
                    MapsActivity.selectedStationMarker = marker;
                    mMap.getUiSettings().setMapToolbarEnabled(true);
                }
                return false;
            }

        });

        //custom window display
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = mActivity.getLayoutInflater().inflate(R.layout.station_info_window, null);

                if (marker.getTag() == null || marker.getTag() instanceof CityItem) {
                    return null;
                }
                StationItem s = (StationItem)marker.getTag();
                String name = s.getName();

                TextView stationName = (TextView) v.findViewById(R.id.station_name);
                TextView stationBikes = (TextView) v.findViewById(R.id.station_bikes_data);
                TextView stationSlots = (TextView) v.findViewById(R.id.station_slots_data);
                TextView stationTime = (TextView) v.findViewById(R.id.station_timestamp_data);

                stationName.setText(name);
                stationBikes.setText("" + s.getFreeBikes());
                stationSlots.setText("" + s.getEmptySlots());
                stationTime.setText(s.getTimestamp().substring(0,10));
                return v;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

            }
        });

        if (mLocation == null) {
            mLocation = new Location("");
            mLocation.setLatitude(40.7128);
            mLocation.setLongitude(74.0059);
        }
        LatLng loc = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_FAR));
        //get data from API endpoint
        AsyncFetchCities asfs = new AsyncFetchCities(mLocation);
        asfs.execute();
    }


    /**
     * adds stations to map using information from the cities marker
     * @param station station object created from JSON output
     */
    public static void addStationToMap(final StationItem station) {
        MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(station.getLoc().getLatitude(), station.getLoc().getLongitude()));
        marker.title(station.getName());
        marker.flat(true);
        //marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_n));
        //color
        float hue = BitmapDescriptorFactory.HUE_GREEN;
        double ratio = 0.0 + station.getFreeBikes() / (0.0 + station.getFreeBikes() + station.getEmptySlots());
        if (station.getFreeBikes() == 0) {
            hue = BitmapDescriptorFactory.HUE_RED;
        } else if (ratio <= 0.33) {
            hue = BitmapDescriptorFactory.HUE_ORANGE;
        } else if (ratio <= 0.66) {
            hue = BitmapDescriptorFactory.HUE_YELLOW;
        }
        marker.icon(BitmapDescriptorFactory.defaultMarker(hue));

        Marker m = mMap.addMarker(marker);
        m.setTag(station);
        STATIONS_ON_MAP.push(m);

    }

    /**
     * adds city to map. Called from AsyncfetchCitie's
     * onPostExecute() method
     * @param city object created from JSON output
     */
    public static void addCitiesToMap(final CityItem city) {
        MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(city.getLoc().getLatitude(), city.getLoc().getLongitude()));
        marker.title(city.getName());
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));


        Marker m = mMap.addMarker(marker);
        m.setTag(city);
    }


    @Override
    public void onLocationChanged(Location location) {
        this.mLocation = location;
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

    @Override
    public void onBackPressed() {
        //zoom logic
        if (mMap != null && selectedStationMarker != null && selectedStationMarker.isInfoWindowShown()) {
            selectedStationMarker.hideInfoWindow();
            mMap.getUiSettings().setMapToolbarEnabled(false);
        } else if (mMap != null && STATIONS_ON_MAP.size() > 0) {
            //remove currently loaded markers
            while (!STATIONS_ON_MAP.isEmpty()) {
                Marker marker = STATIONS_ON_MAP.pop();
                marker.remove();
            }
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_FAR));
            mMap.setMinZoomPreference(DEFAULT_ZOOM_FAR);
        //exit logic
        } else if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }
}
