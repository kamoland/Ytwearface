package com.kamoland.ytwearface.data;

import android.content.Context;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.kamoland.ytwearface.ytconnect.YtConnector;
import com.kamoland.ytwearface.ytconnect.YtSnapshot;
import com.kamoland.ytwearface.SystemUtil;

public class MobileDataListenerService  extends WearableListenerService {
    private static final String QUERY_YTLOGSTATUS_PATH = "/com.kamoland.ytwearface.p7";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        final Context context = getBaseContext();

        String path = messageEvent.getPath();
        if (QUERY_YTLOGSTATUS_PATH.equals(path)) {
            queryYtlogStatus(context);
        }
    }

    private static void queryYtlogStatus(Context context) {
        YtSnapshot sn = new YtSnapshot();
        try {
            // 負荷を減らすため，いきなりsnapshotを問い合わせるのではなく，statusで状態を確認する
            int mode = YtConnector.queryYtTrackingMode(context);

            if (mode == YtConnector.YTTRACKINGMODE_START_1STFIXED) {
                sn = YtConnector.queryYtSnapshot(context, false);
            }

        } catch (YtConnector.YtConnectException ex) {
        }

        sn.mobileBattery = SystemUtil.queryBatteryRemain(context);

        WearIF wif = new WearIF(context, true);
        wif.notifyYtlogStatus(sn);
    }
}