package com.kamoland.ytwearface.ytconnect;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.kamoland.ytwearface.FileUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class YtConnector {
    /** 0:停止中 */
    public static final int YTTRACKINGMODE_STOP = 0;

    /** 1:測定中(初回測位未完了) */
    public static final int YTTRACKINGMODE_START_NOFIX = 1;

    /** 2:測定中(初回測位完了) */
    public static final int YTTRACKINGMODE_START_1STFIXED = 2;

    /**
     * 測定状態問い合わせ
     * @param context
     * @return
     * @throws YtConnectException
     */
    public static int queryYtTrackingMode(Context context) throws YtConnectException {
        String r = query(context, "pub/status");

        if ("0".equals(r)) {
            return YTTRACKINGMODE_STOP;
        } else if ("1".equals(r)) {
            return YTTRACKINGMODE_START_NOFIX;
        } else if ("2".equals(r)) {
            return YTTRACKINGMODE_START_1STFIXED;
        } else {
            throw new YtConnectException();
        }
    }

    /**
     * 測定値問い合わせ
     * @param context
     * @param requireSpeed 速度データが必要かどうか．不要ならfalseを指定した方が高速
     * @return
     * @throws YtConnectException
     */
    public static YtSnapshot queryYtSnapshot(Context context, boolean requireSpeed) throws YtConnectException {
        YtSnapshot sn = new YtSnapshot();
        String contents = query(context, requireSpeed ? "pub/snapshot2" : "pub/snapshot1");
        String[] elem = contents.trim().split(",");
        if (elem.length > 6 && "1".equals(elem[0])) {
            sn.totalDistanceMeter = Integer.parseInt(elem[1]);
            sn.totalTimeSec = Integer.parseInt(elem[2]);
            sn.curSpeedKmh = Float.parseFloat(elem[3]);
            sn.altMeter = Integer.parseInt(elem[4]);
            sn.pressureHpa = Float.parseFloat(elem[5]);
            sn.lastFixedTime = 1000L * Integer.parseInt(elem[6]);
        }
        return sn;
    }

    private static String query(Context context, String path) throws YtConnectException {
        InputStream in = null;
        try {
            in = context.getContentResolver().openInputStream(
                    Uri.parse("content://com.kamoland.ytlog.rp/" + path));
            String t = FileUtil.loadTextFile(in);

            if (TextUtils.isEmpty(t)) {
                throw new YtConnectException();
            }

            return t;

        } catch (FileNotFoundException ex) {
            throw new YtConnectException();

        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception ex) {}
        }
    }

    public static class YtConnectException extends Exception {
    }
}
