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

package io.github.protino.wearable;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import io.github.protino.Utility;
import io.github.protino.data.WeatherContract;

/**
 * An {@link IntentService} subclass to send data items to connected wearable device about the
 * updated weather data using google wearable api.
 */
public class WearableDataService extends Service
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = WearableDataService.class.getSimpleName();
    private GoogleApiClient googleApiClient;

    //Lifecycle start
    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(WearableDataService.this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }
//Lifecycle end

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
        /** Keep the service alive until data_items are sent*/
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "onConnected: wearable api connected");
        new SendUpdates(Utility.getPreferredLocation(this)).start();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //ignore
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "onConnectionFailed: " + connectionResult.toString());
    }


    /**
     * Gracefully disconnects the client
     *
     * @param googleApiClient client that needs to be disconnected
     */
    private void disconnect(GoogleApiClient googleApiClient) {
        if (googleApiClient != null && (googleApiClient.isConnected() || googleApiClient.isConnecting())) {
            googleApiClient.disconnect();
            Log.d(LOG_TAG, "disconnect: client disconnected ");
        }
        stopSelf();
        Log.d(LOG_TAG, "disconnect: service stopped");
    }


    /**
     * Collects data from the database and sends them to the devices. Disconnects when
     * everything is done.
     */
    private class SendUpdates extends Thread {

        private static final int INDEX_WEATHER_ID = 0;
        private static final int INDEX_MAX_TEMP = 1;
        private static final int INDEX_MIN_TEMP = 2;
        private final String PATH = "/wearable_weather_data";
        private final String[] FORECAST_COLUMNS = new String[]{
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
        };
        private final String location;
        private DataMap dataMap = new DataMap();

        private SendUpdates(String location) {
            this.location = location;
        }

        @Override
        public void run() {
            try {
                dataMap = fetchData();
                sendData(PATH, dataMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Fetches weather data for preferred location and builds a json string
         */
        private DataMap fetchData() throws Exception {
            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                    location, System.currentTimeMillis());
            Cursor cursor = getContentResolver().query(
                    weatherForLocationUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                Context context = getApplicationContext();
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                double maxTemp = cursor.getDouble(INDEX_MAX_TEMP);
                double minTemp = cursor.getDouble(INDEX_MIN_TEMP);

                String formattedMaxTemp = Utility.formatTemperature(context, maxTemp);
                String formattedMinTemp = Utility.formatTemperature(context, minTemp);

                dataMap.putInt("weather_id", weatherId);
                dataMap.putString("max_temp", formattedMaxTemp);
                dataMap.putString("min_temp", formattedMinTemp);
                cursor.close();
                return dataMap;
            }
            throw new Resources.NotFoundException("No data found in the database");
        }

        private void sendData(String path, DataMap dataMap) {
            PutDataMapRequest mapRequest = PutDataMapRequest.create(path);
            mapRequest.getDataMap().putAll(dataMap);
            PutDataRequest dataRequest = mapRequest.asPutDataRequest();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient, dataRequest).await();
            if (result.getStatus().isSuccess()) {
                Log.d(LOG_TAG, "sendData: " + dataMap + " successfully sent");
            } else {
                // TODO: 28-Feb-17 Add a mechanism to start the service again with exponential backoff
                Log.e(LOG_TAG, "sendData: Failed to send data item");
            }
            disconnect(googleApiClient);
        }

    }
}
