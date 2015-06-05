package com.example.android.sunshine.app;

import android.content.Intent;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

import com.example.android.sunshine.app.R;

import static android.support.v4.view.MenuItemCompat.*;

public class DetailActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // Get the Intent
        TextView tv = (TextView) findViewById(R.id.weather_value_passed);
        tv.setText(getIntentExtraValue());
    }

    private String getIntentExtraValue () {
        return (String) getIntent().getExtras().get(Intent.EXTRA_TEXT);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        /*
        try {
            // Inflate Share Intent resource file.
            getMenuInflater().inflate(R.menu.shareintent, menu);
            // Locate MenuItem with ShareActionProvider
            MenuItem item = menu.findItem(R.id.share);
            // Fetch and store ShareActionProvider
            ActionProvider actionprovider = MenuItemCompat.getActionProvider(item);
            // Instantiate Share Action Provider
            mShareActionProvider =  (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        } catch (Exception e) {
            Log.e(LOGTAG, "Error Setting the share action provider ...", e);
        }
        */
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } //else if()
        return super.onOptionsItemSelected(item);
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public static final String LOGTAG = DetailActivity.class.getName();

}
