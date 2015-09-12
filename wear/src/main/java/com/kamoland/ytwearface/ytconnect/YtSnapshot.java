package com.kamoland.ytwearface.ytconnect;

import android.text.TextUtils;

public class YtSnapshot {
    /** 現在の速度．(厳密には，直近の3点から求まった速度)．
     * これを取得するには，queryYtSnapshot()をrequireSpeed=trueで呼び出す必要があります
     */
    public float curSpeedKmh;

    /** 合計距離 */
    public int totalDistanceMeter;

    /** 合計経過時間 */
    public int totalTimeSec;

    /** 最後に位置が確定した時の標高．(ジオイド高補正済み) */
    public int altMeter;

    /** 最後に位置が確定した時の気圧．(対応端末のみ) */
    public float pressureHpa;

    /** 最後に位置が確定した時刻(long値) */
    public long lastFixedTime;

    /** 現在の母艦のバッテリー残量．(0-100) */
    public int mobileBattery;

    public String toPlainString() {
        StringBuilder sb = new StringBuilder();
        sb.append(curSpeedKmh)
                .append("\n").append(totalDistanceMeter)
                .append("\n").append(totalTimeSec)
                .append("\n").append(altMeter)
                .append("\n").append(pressureHpa)
                .append("\n").append(lastFixedTime)
                .append("\n").append(mobileBattery);
        return sb.toString();
    }

    public void parsePlainString(String val) {
        if (TextUtils.isEmpty(val)) {
            return;
        }
        String[] el = TextUtils.split(val, "\n");
        curSpeedKmh = Float.parseFloat(el[0]);
        totalDistanceMeter = Integer.parseInt(el[1]);
        totalTimeSec = Integer.parseInt(el[2]);
        altMeter = Integer.valueOf(el[3]);
        pressureHpa = Float.valueOf(el[4]);
        lastFixedTime = Long.parseLong(el[5]);
        mobileBattery = Integer.parseInt(el[6]);
    }
}
