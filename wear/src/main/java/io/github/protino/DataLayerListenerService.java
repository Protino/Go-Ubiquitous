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
 * Listens for data layer changes. Saves new data in {@link SharedPreferences}.
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
                Log.d(LOG_TAG, "onDataChanged: ");
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
