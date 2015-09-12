package com.kamoland.ytwearface.data;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.kamoland.ytwearface.MyWatchFaceService;

public class WearDataListenerService  extends WearableListenerService {
    private static final String QUERY_YTLOGSTATUS_RESULT_PATH = "/com.kamoland.ytwearface.p103";
    private static final String PARAM_1 = "p1";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            DataItem dataItem = event.getDataItem();
            final String path = dataItem.getUri().getPath();
            DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();

            if (QUERY_YTLOGSTATUS_RESULT_PATH.equals(path)) {
                String res = dataMap.getString(PARAM_1);

                Intent intent = new Intent(MyWatchFaceService.ACTION_UPDATE);
                intent.putExtra(MyWatchFaceService.INTENTPARAM_PARAM1, res);
                sendBroadcast(intent);
            }
        }
    }
}
