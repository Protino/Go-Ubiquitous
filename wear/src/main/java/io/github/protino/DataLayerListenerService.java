package io.github.protino;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Gurupad Mamadapur on 27-Feb-17.
 */

public class DataLayerListenerService extends WearableListenerService {
    private static final String LOG_TAG = DataLayerListenerService.class.getSimpleName();

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(LOG_TAG, "DataItem deleted: " + event.getDataItem());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(LOG_TAG, "DataItem changed: " + event.getDataItem());
            }
        }
    }
}
