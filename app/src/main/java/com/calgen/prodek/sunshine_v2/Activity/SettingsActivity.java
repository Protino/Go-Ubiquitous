package com.calgen.prodek.sunshine_v2.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.calgen.prodek.sunshine_v2.R;
import com.calgen.prodek.sunshine_v2.Utility;
import com.calgen.prodek.sunshine_v2.data.WeatherContract;
import com.calgen.prodek.sunshine_v2.sync.SunshineSyncAdapter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Gurupad on 15-Jun-16.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public final static int PLACE_PICKER_REQUEST = 9090;
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private ImageView attribution;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_icon_pack_key)));
        bindPreferenceSummaryToValue(findPreference(getResources().getString(R.string.pref_temperature_key)));

        attribution = new ImageView(this);
        attribution.setPadding(16,16,16,16);
        attribution.setImageResource(R.drawable.powered_by_google_light);
        if (!Utility.isLocationLatLonAvailable(this)) {
            attribution.setVisibility(View.GONE);
        }
        setListFooter(attribution);


        CheckBoxPreference notificationPreference = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_notification_key));
        notificationPreference.setOnPreferenceChangeListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            if (!(preference instanceof CheckBoxPreference)) {
                if (preference instanceof EditTextPreference) {
                    @SunshineSyncAdapter.LocationStatus int status = Utility.getLocationStatus(this);
                    switch (status) {
                        case SunshineSyncAdapter.LOCATION_STATUS_OK:
                            break;
                        case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                            stringValue = String.format(getString(R.string.pref_location_error_description), stringValue);
                            break;
                        default:
                            stringValue = String.format(getString(R.string.pref_location_unknown_description), stringValue);
                            break;
                    }
                }
                preference.setSummary(stringValue);
            }

        }
        return true;
    }

    @Override
    protected void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_key))) {
            // we've changed the location
            // Wipe out any potential PlacePicker latlng values so that we can use this text entry.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(getString(R.string.pref_location_latitude));
            editor.remove(getString(R.string.pref_location_longitude));
            editor.commit();

            if (attribution != null) attribution.setVisibility(View.GONE);

            Utility.resetLocationStatus(this);
            SunshineSyncAdapter.syncImmediately(this);
        } else if (key.equals(getString(R.string.pref_temperature_key))) {
            // units have changed. update lists of weather entries accordingly
            getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        } else if (key.equals(getString(R.string.pref_location_status_key))) {
            // our location status has changed.  Update the summary accordingly
            Preference locationPreference = findPreference(getString(R.string.pref_location_key));
            bindPreferenceSummaryToValue(locationPreference);
        } else if (key.equals(getString(R.string.pref_icon_pack_key))) {
            // art pack have changed. update lists of weather entries accordingly
            getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check to see if the result is from our Place Picker intent
        if (requestCode == PLACE_PICKER_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String address = place.getAddress().toString();
                LatLng latLong = place.getLatLng();

                if (TextUtils.isEmpty(address)) {
                    address = String.format("(%.2f, %.2f)", latLong.latitude, latLong.longitude);
                }

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.pref_location_key), address);
                editor.putFloat(getString(R.string.pref_location_latitude), (float) latLong.latitude);
                editor.putFloat(getString(R.string.pref_location_longitude), (float) latLong.longitude);
                editor.commit();

                Preference locationPreference = findPreference(getString(R.string.pref_location_key));
                locationPreference.setSummary(address);

                if (attribution!=null){
                    attribution.setVisibility(View.VISIBLE);
                }

                Utility.resetLocationStatus(this);
                SunshineSyncAdapter.syncImmediately(this);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
