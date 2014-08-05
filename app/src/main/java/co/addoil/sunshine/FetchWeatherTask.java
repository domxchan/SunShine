package co.addoil.sunshine;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import co.addoil.sunshine.data.WeatherContract;

/**
 * Created by chandominic on 5/8/14.
 */
class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    public static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private static final boolean DEBUG = true;
    private Context mContext;

    public FetchWeatherTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        if (params.length == 0) return null;

        String locationQuery = params[0];
        String forecastJsonStr = null;
        String format = "json";
        String units = "metric";
        int numDays = 14;

        try {
            UrlFetcher fetcher = new UrlFetcher();
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri uri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, String.valueOf(numDays))
                    .build();

            String jsonString = fetcher.getUrlString(uri.toString());
            String[] results = getWeatherDataFromJson(jsonString, 7, locationQuery);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Failed! IOException.");
            ioe.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private long addLocation(String locationSetting, String cityName, double lat, double lon) {

        Log.v(LOG_TAG, "inserting " + cityName + ", with coord: " + lat + ", " + lon);

        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_POSTAL_CODE + " = ?",
                new String[]{locationSetting},
                null);

        if (cursor.moveToFirst()) {
            Log.v(LOG_TAG, "Found it in the database!");
            int locationIdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            return cursor.getLong(locationIdIndex);
        } else {
            Log.v(LOG_TAG, "Didn't find it in the database, inserting now!");
            ContentValues values = new ContentValues();
            values.put(WeatherContract.LocationEntry.COLUMN_POSTAL_CODE, locationSetting);
            values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_NAME, cityName);

            Uri uri = mContext.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    values
            );

            return ContentUris.parseId(uri);

        }
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy: constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays, String locationSetting)
            throws JSONException {

        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_COORD_LAT = "lat";
        final String OWM_COORD_LONG = "long";

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        String[] resultStrs = null;

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = coordJSON.getLong(OWM_COORD_LAT);
            double cityLongitude = coordJSON.getLong(OWM_COORD_LONG);

            Log.v(LOG_TAG, cityName + ", with coord: " + cityLatitude + " " + cityLongitude);

            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());
            resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                String day;
                String description;
                int weatherId;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long. We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                dateTime = dayForecast.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                // Temperatures are in a child object called "temp". Try not to name variables
                // "temp" when working with temperature. It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                        WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);

                cVVector.add(weatherValues);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                int rowsInserted = mContext.getContentResolver()
                        .bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
                Log.v(LOG_TAG, "inserted " + rowsInserted + " rows of weather data");

                // Use a DEBUG variable to gate whether or not you do this, so you can easily
                // turn it on and off, and so that it's easy to see what you can rip out if
                // you ever want to remove it.
                if (DEBUG) {
                    Cursor weatherCursor = mContext.getContentResolver().query(
                            WeatherContract.WeatherEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null
                    );

                    if (weatherCursor.moveToFirst()) {
                        ContentValues resultValues = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(weatherCursor, resultValues);
                        Log.v(LOG_TAG, "Query succeeded! **********");
                        for (String key : resultValues.keySet()) {
                            Log.v(LOG_TAG, key + ": " + resultValues.getAsString(key));
                        }
                    } else {
                        Log.v(LOG_TAG, "Query failed! :( **********");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return resultStrs;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
    * so for convenience we're breaking it out into its own method now.
    */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String units = pref.getString(
                mContext.getString(R.string.pref_temp_units_key),
                mContext.getString(R.string.pref_temp_units_metric));

        if (units.equals(mContext.getString(R.string.pref_temp_units_imperial))) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

}
