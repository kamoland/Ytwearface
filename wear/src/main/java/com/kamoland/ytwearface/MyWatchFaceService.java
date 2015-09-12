package com.kamoland.ytwearface;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.kamoland.ytwearface.data.MobileIF;
import com.kamoland.ytwearface.ytconnect.YtSnapshot;


public class MyWatchFaceService extends CanvasWatchFaceService {
    public static final String ACTION_UPDATE = "com.kamoland.ytwearface.ITEMUPDATE";
    public static final String INTENTPARAM_PARAM1 = "p1";

    private static final boolean UPDATE_WHILE_INVISIBLE = true;

    private String[] WEEKDAY_DESC;

    private static boolean isDebugMode;

    private DisplayDataReceiver dataReceiver;
    private boolean registeredMyReceiver;

    private long lastUpdateRequest;

    private YtSnapshot dataItem = new YtSnapshot();

    private Engine engine;

    @Override
    public Engine onCreateEngine() {
        super.onCreateEngine();
        isDebugMode = DeployUtil.isDebuggable(getApplicationContext());
        log("onCreateEngine");

        WEEKDAY_DESC = getResources().getStringArray(R.array.weekday_desc);

        registerMyReceiver();

        engine = new Engine();
        return engine;
    }

    @Override
    public void onDestroy() {
        log("onDestroy");
        unregisterMyReceiver();
        super.onDestroy();
    }

    public void updateFromMobile(Intent intent) {
        String res = intent.getStringExtra(INTENTPARAM_PARAM1);
        log("updateFromMobile:" + res);

        if (ACTION_UPDATE.equals(intent.getAction())) {
            // 盤面更新
            if (dataItem != null && engine != null && engine.isVisible()) {
                dataItem.parsePlainString(res);
                engine.invalidate();
            }
        }
    }

    private void registerMyReceiver() {
        if (registeredMyReceiver) {
            return;
        }
        registeredMyReceiver = true;
        dataReceiver = new DisplayDataReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE);
        MyWatchFaceService.this.registerReceiver(dataReceiver, filter);
        log("registerMyReceiver finish.");
    }

    private void unregisterMyReceiver() {
        if (!registeredMyReceiver) {
            return;
        }
        registeredMyReceiver = false;
        MyWatchFaceService.this.unregisterReceiver(dataReceiver);
        log("unregisterMyReceiver finish.");
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private Context context;

        private Paint linePaint;
        private Paint paintR1L;
        private Paint paintR1R;
        private Paint paintR2L;
        private Bitmap altBmp;
        private Bitmap mobileBatteryBmp;
        private Bitmap wearBatteryBmp;

        private boolean isRoundScreen;
        private boolean isLowBitAmbient;
        private boolean isBurnInProtection;

        private Time mTime;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            log("onCreate");
            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            this.context = getApplicationContext();

            changeColorForAmbient();

            mTime = new Time();

            doUpdate();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            log("onApplyWindowInsets");
            super.onApplyWindowInsets(insets);
            isRoundScreen = insets.isRound();
        }

        private void changeColorForAmbient() {
            boolean isAmbient = isInAmbientMode();

            if (linePaint == null) {
                return;
            }
            int textColor;
            if (isAmbient) {
                // アンビエント用の設定
                if (isLowBitAmbient) {
                    // 色数制限モード．端末側で暗くしてくれる (SmartWatch3)
                    linePaint.setColor(Color.WHITE);
                    linePaint.setStrokeWidth(1);
                    textColor = Color.WHITE;
                } else {
                    // 明示的に暗くする (G Watch R)
                    linePaint.setColor(Color.parseColor("#507050"));
                    linePaint.setStrokeWidth(2);
                    textColor = Color.parseColor("#909090");
                }

            } else {
                // 対話モード
                linePaint.setColor(Color.BLACK);
                linePaint.setStrokeWidth(3);
                textColor = Color.WHITE;
            }

            paintR1L.setColor(textColor);
            paintR1R.setColor(textColor);
            paintR2L.setColor(textColor);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            isLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            isBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            log("onPropertiesChanged:" + isLowBitAmbient + "," + isBurnInProtection);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            log("onTimeTick: ambient = " + isInAmbientMode());

            if (UPDATE_WHILE_INVISIBLE || isVisible()) {
                doUpdate();
            }
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            log("onAmbientModeChanged:" + inAmbientMode);

            changeColorForAmbient();
            invalidate();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            log("onVisibilityChanged:" + visible);

            if (visible && UPDATE_WHILE_INVISIBLE) {
                doUpdate();
            }
        }

        private void doUpdate() {
            long t = System.currentTimeMillis();
            if (t < lastUpdateRequest + 1000*5) {
                // 5秒以上空ける
                return;
            }
            lastUpdateRequest = t;

            // 母艦と切断されていても，時刻だけは更新されるようにする
            invalidate();

            MobileIF mobIf = new MobileIF(getApplicationContext(), true);
            mobIf.queryYtlogStatus();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();

            int width = bounds.width();
            int height = bounds.height();
            int centerX = width / 2;
            int centerY = height / 2;

            // 丸形では上下の余白を大きく取る
            int VERTICAL_PADDING = isRoundScreen? 20: 10;

            int X_PADDING = 7;
            int X_ICON_PADDING = 28;

            int x1 = (int)(width * 0.6);

            int y0 = VERTICAL_PADDING;
            int vheight = height - VERTICAL_PADDING * 2;
            int heightS = (int)(vheight / 3.5 * 1);
            int heightL = (int)(vheight / 3.5 * 1.5);
            int y1 = y0 + heightS;
            int y2 = y1 + heightL;
            int y3 = y0 + vheight;

            int textSizeS = (int)(heightS / 2.5);
            int textSizeL = (int)(heightL / 2);

            // 円周において横幅を確保するため，ぎりぎりまで下げる
            int y1F = y1 - 5;
            int y2F = y2 - (heightL - textSizeL) / 2;
            // 円周において横幅を確保するため，ぎりぎりまで上げる
            int y3F = y3 - heightS + textSizeS + 3;

            int y2uF = y2F - textSizeS - 2;

            if (linePaint == null) {
                linePaint = new Paint();

                paintR1L = new Paint();
                paintR1L.setTypeface(Typeface.DEFAULT);
                paintR1L.setTextSize(textSizeS);
                paintR1L.setTextAlign(Paint.Align.RIGHT);

                paintR1R = new Paint();
                paintR1R.setTypeface(Typeface.DEFAULT);
                paintR1R.setTextSize(textSizeS);
                paintR1R.setTextAlign(Paint.Align.LEFT);

                paintR2L = new Paint();
                paintR2L.setTypeface(Typeface.SANS_SERIF);
                paintR2L.setTextSize(textSizeL);
                paintR2L.setTextAlign(Paint.Align.RIGHT);

                changeColorForAmbient();

                altBmp = ((BitmapDrawable)getResources().getDrawable(R.drawable.alt)).getBitmap();
                mobileBatteryBmp = ((BitmapDrawable)getResources().getDrawable(R.drawable.battery)).getBitmap();
                wearBatteryBmp = ((BitmapDrawable)getResources().getDrawable(R.drawable.wear_battery)).getBitmap();
            }

            boolean isAmbientMode = isInAmbientMode();

            canvas.drawColor(isAmbientMode? Color.BLACK: Color.parseColor("#206020"));
            canvas.drawLine(0, y0, width, y0, linePaint);
            canvas.drawLine(0, y1, width, y1, linePaint);
            canvas.drawLine(0, y2, width, y2, linePaint);
            canvas.drawLine(0, y3, width, y3, linePaint);
            canvas.drawLine(centerX, y0, centerX, y1, linePaint);
            canvas.drawLine(centerX, y2, centerX, y3, linePaint);

            String nowTime = mTime.hour + ":" + formatd2(mTime.minute);
            String nowDay = String.valueOf(mTime.monthDay) + " " + WEEKDAY_DESC[mTime.weekDay];

            int totalTimeSec = dataItem.totalTimeSec;
            String totalTimeDesc = getElapsedDesc(totalTimeSec);

            String distDesc;
            int distanceMeter = dataItem.totalDistanceMeter;
            if (distanceMeter >= 10000) {
                // 10kmを越えた場合はkm単位にする
                distDesc = formatNumber((float)distanceMeter / 1000) + "km";
            } else {
                distDesc = String.valueOf(distanceMeter) + "m";
            }

            String speedDesc;
            float speedKmh = dataItem.totalDistanceMeter * 3.6f / dataItem.totalTimeSec;
            if (speedKmh >= 100) {
                // 100km/hを超えた場合は小数以下を切り捨てる
                speedDesc = (int)(speedKmh) + "km/h";
            } else {
                speedDesc = formatNumber(speedKmh) + "km/h";
            }

            String mobileBatteryDesc = String.valueOf(dataItem.mobileBattery);
            String altDesc = dataItem.altMeter + "m";
            String wearBatteryDesc = String.valueOf(SystemUtil.queryBatteryRemain(context));

            boolean useYtData = (totalTimeSec > 0);

            canvas.drawText(nowTime, x1 - X_PADDING, y2F, paintR2L);
            canvas.drawText(nowDay, x1 + X_PADDING, y2uF, paintR1R);
            if (!isAmbientMode || !isBurnInProtection) {
                // アンビエントかつ焼き付き防止モードの場合はアイコンを表示しない
                // 15x27
                canvas.drawBitmap(mobileBatteryBmp, centerX + X_PADDING + 4, y3F - 25, null);
            }
            canvas.drawText(mobileBatteryDesc, centerX + X_PADDING + X_ICON_PADDING, y3F, paintR1R);
            if (!isAmbientMode || !isBurnInProtection) {
                // 16x27
                canvas.drawBitmap(wearBatteryBmp, centerX + X_PADDING + 4, y3F + textSizeS - 25, null);
            }
            canvas.drawText(wearBatteryDesc, centerX + X_PADDING + X_ICON_PADDING, y3F + textSizeS, paintR1R);

            if (useYtData) {
                canvas.drawText(distDesc, centerX - X_PADDING, y1F, paintR1L);
                if (isRoundScreen) {
                    canvas.drawText(speedDesc, centerX + X_PADDING, y1F, paintR1R);
                } else {
                    // 角型では右に寄せる
                    canvas.drawText(speedDesc, width - 3, y1F, paintR1L);
                }

                canvas.drawText(totalTimeDesc, x1 + X_PADDING, y2F + 4, paintR1R);

                if (!isAmbientMode || !isBurnInProtection) {
                    // 14x14
                    canvas.drawBitmap(altBmp, centerX - X_PADDING - 18, y3F - 14,  null);
                }
                canvas.drawText(altDesc, centerX - X_PADDING - X_ICON_PADDING, y3F, paintR1L);
            }
        }
    }

    private static String formatNumber(float f) {
        // String.format("%.1f", f) と同等
        String s = String.valueOf(f + 0.05);
        int i = s.indexOf(".");
        if (i < 0) {
            return s + ".0";
        } else {
            return s.substring(0, i + 2);
        }
    }

    private static String getElapsedDesc(int sec) {
        String elapsedDesc;
        String sign;
        if (sec >= 0) {
            sign = "+";
        } else {
            sign = "-";
            sec = -sec;
        }
        int d = sec / 86400;
        sec -= d * 86400;
        int h = sec / 3600;
        sec -= h * 3600;
        int m = sec / 60;
        sec -= m * 60;

        elapsedDesc = sign + (d > 0? String.valueOf(d) + ":": "") + h + ":" + formatd2(m);

        return elapsedDesc;
    }

    private static String formatd2(int d) {
        // String.format("%02d", d) と同等
        if (d < 10) {
            return "0" + d;
        }
        return String.valueOf(d);
    }

    private static void log(String mes) {
        if (isDebugMode) {
            Log.d("**MyWatchFaceService", mes);
        }
    }
}
