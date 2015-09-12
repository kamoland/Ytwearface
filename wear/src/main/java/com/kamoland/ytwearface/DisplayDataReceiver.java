package com.kamoland.ytwearface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DisplayDataReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MyWatchFaceService.ACTION_UPDATE.equals(intent.getAction())) {
            MyWatchFaceService service = (MyWatchFaceService) context;
            service.updateFromMobile(intent);
        }
    }
}
