/*
 *    Copyright 2016 Gurupad Mamadapur
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.protino.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import io.github.protino.R;
import io.github.protino.Utility;
import io.github.protino.adapter.ForecastAdapter;
import io.github.protino.data.WeatherContract;
import io.github.protino.fragment.DetailFragment;
import io.github.protino.fragment.ForecastFragment;
import io.github.protino.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private static final String PANE_TYPE = "pane_type";
    private static boolean mTwoPane;
    private String mLocation;

    //Lifecycle start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mLocation = Utility.getPreferredLocation(this);

        Uri contentUri = getIntent() != null ? getIntent().getData() : null;

        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                DetailFragment detailFragment = new DetailFragment();
                if (contentUri != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(DetailFragment.DETAIL_URI, contentUri);
                    detailFragment.setArguments(bundle);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        ForecastFragment forecastFragment = (ForecastFragment) (getSupportFragmentManager().findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane);
        if (contentUri != null) {
            forecastFragment.setInitialSelectedDate(
                    WeatherContract.WeatherEntry.getDateFromUri(contentUri)
            );
        }
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
            }
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (null != df) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }
//Lifecycle end

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(PANE_TYPE, mTwoPane);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_map:
                openPreferredLocationInMap();
                return true;
            case R.id.action_refresh:
                SunshineSyncAdapter.syncImmediately(this);
                return true;
            default: //ignore
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String preferredLocation = sharedPreferences.getString(
                getResources().getString(R.string.pref_location_key),
                getResources().getString(R.string.pref_location_default));

        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + preferredLocation);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Log.e(TAG, "Couldn't launch the map activity");
        }
    }

    @Override
    public void onItemSelected(Uri dateUri, ForecastAdapter.ViewHolder viewHolder) {
        if (mTwoPane) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailFragment.DETAIL_URI, dateUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.setData(dateUri);
            ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this
                            , new Pair<View, String>(viewHolder.mIconView, getString(R.string.detail_icon_transition_name)));
            ActivityCompat.startActivity(this, intent, activityOptionsCompat.toBundle());
        }
    }
}
