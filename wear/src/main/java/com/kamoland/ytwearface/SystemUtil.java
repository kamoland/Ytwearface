package com.kamoland.ytwearface;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class SystemUtil {
    public static int queryBatteryRemain(Context context) {
        Intent bat = context.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = bat.getIntExtra("level", 0);
        int scale = bat.getIntExtra("scale", 100);
        int batt = level * 100 / scale;

        return batt;
    }
}
