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
import edu.gatech.mmccoy37.maptest.pojo.StationItem;

/**
 * grabs nearby stations to user by parsing json objects
 * retrieved from API endpoint
 *
 * @author matt
 */
public class AsyncFetchStations extends AsyncTask<String, Void, Integer> {

    private HashMap<Location, StationItem> stationMap;
    private static final String API_URL_BASE = "http://api.citybik.es";

    public AsyncFetchStations() {
        super();
        stationMap = new HashMap<>();
    }


    private void parseStations(String json) {
        //get cities from API endpoint
        try {
            JSONObject response = new JSONObject(json);
            JSONObject network = response.optJSONObject("network");
            JSONArray stations = network.optJSONArray("stations");

            for (int i = 0; i < stations.length(); i++) {
                JSONObject s = stations.getJSONObject(i);
                StationItem station = new StationItem();
                station.setEmptySlots(s.optInt("empty_slots"));
                station.setFreeBikes(s.optInt("free_bikes"));
                station.setName(s.optString("name"));
                station.setID(s.optString("id"));
                station.setTimestamp(s.optString("timestamp"));
                station.setAddress(s.getJSONObject("extra").optString("address"));
                Location loc = new Location("");
                loc.setLatitude(s.optDouble("latitude"));
                loc.setLongitude(s.optDouble("longitude"));
                station.setLoc(loc);

                stationMap.put(loc, station);
                Log.d("API_TAG", "added station: " + station.getName());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected Integer doInBackground(String... params) {
        Integer result = 1;
        HttpURLConnection urlConnection;
        try {
            //get all cities from API
            URL url = new URL(API_URL_BASE + params[0]);
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
                parseStations(response.toString());
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
        //MapsActivity.setStationMap(stationMap);
        for (StationItem s: stationMap.values()) {
            MapsActivity.addStationToMap(s);
        }
    }
}
