package com.calgen.prodek.sunshine_v2.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.calgen.prodek.sunshine_v2.R;
import com.calgen.prodek.sunshine_v2.Utility;
import com.calgen.prodek.sunshine_v2.data.WeatherContract;
import com.calgen.prodek.sunshine_v2.sync.SunshineSyncAdapter;

/**
 * Created by Gurupad on 15-Jun-16.
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

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
            Utility.resetLocationStatus(this);
            SunshineSyncAdapter.syncImmediately(this);
        } else if (key.equals(getString(R.string.pref_temperature_key))) {
            getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        }
    }
}
