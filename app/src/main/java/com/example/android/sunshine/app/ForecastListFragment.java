package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.utils.JsonParserUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

/**
 * Created by prolap on 01/06/15.
 */
public class ForecastListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_ADAPTER = 0;

    private static final String[] FORECAST_COLUMNS = {
        WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
        WeatherContract.WeatherEntry.COLUMN_DATE,
        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
        WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
        WeatherContract.LocationEntry.COLUMN_COORD_LAT,
        WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    CursorAdapter mForecastAdapter = null;

    public ForecastListFragment() {
        Log.d(LOGTAG, "ForecastListFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tell the fragment that menu options are available
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOGTAG, "On Create View ...");
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        /*
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        Log.w(LOGTAG, "Week Forecast --> " + weekForecast.size());
        */

        final ArrayAdapter<String> forecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        new ArrayList());

        // Get a reference to the ListView, and attach this adapter to it.
        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);
        // Set Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String weatherforecast = forecastAdapter.getItem(position);
                Toast toast = Toast.makeText(getActivity(), weatherforecast, Toast.LENGTH_SHORT);
                toast.show();
                // Replacing Toast with Intent
                Intent detailActivityIntent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, weatherforecast);
                startActivity(detailActivityIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
        inflater.inflate(R.menu.mapmenu, menu);
    }


    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean returnvalue = true;
        try {
            if (item.getItemId() == R.id.action_refresh) {
                // Handling Shared Preferences ...
                updateWeather();
                returnvalue = true;
            } else if (item.getItemId() == R.id.mapmenu) {
                Toast.makeText(getActivity(), "Map Menu Clicked ...", Toast.LENGTH_SHORT).show();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String geouri = "geo:0,0?q=" + prefs.getString(getString(R.string.pref_location_key), "94043");
                showMap(Uri.parse(geouri));
            } else {
               returnvalue =  super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            Log.e(LOGTAG , "Unknown Exception", e);
        } finally {
        }
        return returnvalue;
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void updateWeather() {
        ArrayAdapter<String> forecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        new ArrayList());
        FetchWeatherTask fwt = new FetchWeatherTask(getActivity(), forecastAdapter);
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //String locationZipCode = prefs.getString(getString(R.string.pref_location_key), "94043");
        //String metric = prefs.getString(getString(R.string.pref_metric_key), "metric");
        fwt.execute(null, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    private class AsyncNetworkOperation extends AsyncTask<String, Void, String[]> {


        @Override
        protected String[] doInBackground(String... ineturl) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Json Object
            String [] jsonValueArray = null;
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(ineturl [0]);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
                // Parse Json
                JsonParserUtils jsonparser = new JsonParserUtils();
                jsonValueArray = jsonparser.getWeatherDataFromJson(forecastJsonStr, 7);
            } catch (IOException ioe) {
                Log.e(LOGTAG, "Error ", ioe);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } catch (Exception e) {
                Log.e(LOGTAG , "Unknown Exception", e);
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            return jsonValueArray;
        }

        protected void onPostExecute(String[] jsonValueArray) {
            final ArrayAdapter<String> forecastAdapter =
                    new ArrayAdapter<String>(
                            getActivity(), // The current context (this activity)
                            R.layout.list_item_forecast, // The name of the layout ID.
                            R.id.list_item_forecast_textview, // The ID of the textview to populate.
                            jsonValueArray);

            listView.setAdapter(forecastAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String weatherforecast = forecastAdapter.getItem(position);
                    Toast toast = Toast.makeText(getActivity(), weatherforecast, Toast.LENGTH_SHORT);
                    toast.show();
                    // Replacing Toast with Intent
                    Intent detailActivityIntent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, weatherforecast);
                    startActivity(detailActivityIntent);
                }
            });
        }
    }

    ListView listView = null;

    public static final String LOGTAG = ForecastListFragment.class.getName();

}
