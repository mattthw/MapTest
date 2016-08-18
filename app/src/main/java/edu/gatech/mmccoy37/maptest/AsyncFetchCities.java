package edu.gatech.mmccoy37.maptest;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import edu.gatech.mmccoy37.maptest.pojo.CityItem;

/**
 * grabs nearby stations to user by parsing json objects
 * retrieved from API endpoint
 *
 * @author matt
 */
public class AsyncFetchCities extends AsyncTask<String, Void, Integer> {

    private HashMap<Location, CityItem> cityMap;
    private static final String API_URL = "http://api.citybik.es/v2/networks";
    private Location userLocation;

    public AsyncFetchCities() {
        super();
    }

    public AsyncFetchCities(Location loc) {
        this.userLocation = loc;
    }

    public HashMap<Location, CityItem> getCityMap() {
        return cityMap;
    }

    public void setCityMap(HashMap<Location, CityItem> cityMap) {
        this.cityMap = cityMap;
    }


    private HashMap<Location, CityItem> parseCities(String json) {
        HashMap<Location, CityItem> cities = new HashMap<>();
        //get cities from API endpoint
        try {
            JSONObject response = new JSONObject(json);
            JSONArray networks = response.optJSONArray("networks");

            for (int i = 0; i < networks.length(); i++) {
                JSONObject network = networks.optJSONObject(i);
                CityItem city = new CityItem();
                city.setID(network.optString("id"));
                city.setHref(network.optString("href"));

                JSONObject networkLoc = network.optJSONObject("location");
                    city.setName(networkLoc.optString("city"));
                    Location loc = new Location("");
                    loc.setLatitude(networkLoc.optDouble("latitude"));
                    loc.setLongitude(networkLoc.optDouble("longitude"));
                    city.setLoc(loc);
                cities.put(loc,city);
                Log.d("API_TAG", "added city:" + city.getName());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cities;
    }

    private void parseStations(Location center, String json, float dist) {
        //HashMap<Location, StationItem> stationsMap = null;
        //get cities from API endpoint
        //try {
        //    JSONObject response = new JSONObject(json);
        //    JSONObject network = response.optJSONObject("network");
        //    JSONArray stations = network.optJSONArray("stations");
        //
        //    for (int i = 0; i < stations.length(); i++) {
        //        JSONObject s = stations.getJSONObject(i);
        //        StationItem station = new StationItem();
        //        station.setEmptySlots(s.optInt("empty_slots"));
        //        station.setFreeBikes(s.optInt("free_bikes"));
        //        station.setName(s.optString("name"));
        //        station.setID(s.optString("id"));
        //        station.setTimestamp(s.optString("timestamp"));
        //        station.setAddress(s.getJSONObject("extra").optString("address"));
        //        Location loc = new Location("");
        //        loc.setLatitude(s.optDouble("latitude"));
        //        loc.setLongitude(s.optDouble("longitude"));
        //        station.setLoc(loc);
        //
        //        //only get station info if it is close to user's location
        //        if (loc.distanceTo(center) < dist) {
        //            this.stationMap.put(loc, station);
        //            Log.d("API_TAG", "added station: " + station.getName());
        //        }
        //    }
        //} catch (JSONException e) {
        //    e.printStackTrace();
        //}
    }



    @Override
    protected Integer doInBackground(String... params) {
        Integer result = 1;
        HttpURLConnection urlConnection;
        try {
            //get all cities from API
            Location center = this.userLocation;
            URL url = new URL(API_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            int statusCode = urlConnection.getResponseCode();
            // 200 represents HTTP OK
            if (statusCode == 200) {
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    response.append(line);
                }
                this.cityMap = parseCities(response.toString());
                if (getCityMap() == null) {
                    result = 0;
                }
            } else {
                result = 0;
            }
        } catch (Exception e) {
            Log.d("API", e.getLocalizedMessage());
        }
        return result; //"Failed to fetch data!";
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        MapsActivity.mMap.clear();
        //MapsActivity.setStationMap(this.stationMap);
        for (CityItem i: cityMap.values()) {
            MapsActivity.addCitiesToMap(i);
        }
    }
}
