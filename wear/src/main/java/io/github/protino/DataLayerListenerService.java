package io.github.protino;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Gurupad Mamadapur on 27-Feb-17.
 */

public class DataLayerListenerService extends WearableListenerService {
    private static final String LOG_TAG = DataLayerListenerService.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    public static String weatherIdPrefKey;
    public static String maxTempPrefKey;
    public static String minTempPrefKey;

    //Lifecycle start
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        weatherIdPrefKey = getString(R.string.WEATHER_ID_PREF_KEY);
        maxTempPrefKey = getString(R.string.MAX_TEMP_PREF_KEY);
        minTempPrefKey = getString(R.string.MIN_TEMP_PREF_KEY);
    }
//Lifecycle end

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(LOG_TAG, "DataItem deleted: " + event.getDataItem());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(weatherIdPrefKey, dataMap.getInt(weatherIdPrefKey));
                editor.putString(maxTempPrefKey, dataMap.getString(maxTempPrefKey));
                editor.putString(minTempPrefKey, dataMap.getString(minTempPrefKey));
                editor.apply();
            }
        }
    }
}
