package co.addoil.sunshine;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chandominic on 23/7/14.
 */
public class WeatherDataParser {

    final static String TAG = "WeatherDataParser";
    private Context mContext;

    public WeatherDataParser(Context context) {
        mContext = context;
    }

    /**
     * Given a string of the form returned by the api call:
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * retrieve the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     */
    public double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {

        JSONObject weatherJsonArray = new JSONObject(weatherJsonStr);
        JSONArray dailyforecastJsonArray = weatherJsonArray.getJSONArray("list");
        JSONObject dayforecastJsonObject = dailyforecastJsonArray.getJSONObject(dayIndex);
        JSONObject dayTempJsonObject = dayforecastJsonObject.getJSONObject("temp");
        double maxTemp = dayTempJsonObject.getDouble("max");
        return maxTemp;
    }

}
