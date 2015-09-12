package com.kamoland.ytwearface.data;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.kamoland.ytwearface.ytconnect.YtSnapshot;

public class WearIF {
    private static final String QUERY_YTLOGSTATUS_RESULT_PATH = "/com.kamoland.ytwearface.p103";
    private static final String PARAM_NOTIFICATION_TIME = "pt";
    private static final String PARAM_1 = "p1";

    private Context context;

    private GoogleApiClient mGoogleApiClient;
    private boolean autoClose;

    public WearIF(Context context, boolean autoCloseAfterSend) {
        this.context = context;
        autoClose = autoCloseAfterSend;
    }

    public void notifyYtlogStatus(YtSnapshot sn) {
        startSend(QUERY_YTLOGSTATUS_RESULT_PATH, sn.toPlainString());
    }

    private void startSend(final String path, final String mes) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            send(mGoogleApiClient, path, mes, new Runnable() {
                @Override
                public void run() {
                    if (autoClose) {
                        finish();
                    }
                }
            });
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        send(mGoogleApiClient, path, mes, new Runnable() {
                            @Override
                            public void run() {
                                if (autoClose) {
                                    finish();
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                }).build();
        mGoogleApiClient.connect();
    }

    public void finish() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    private static void send(GoogleApiClient mGoogleApiClient, final String path, final String mes,
                             final Runnable callback) {
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(path);
        dataMapRequest.getDataMap().putString(PARAM_1, mes);
        dataMapRequest.getDataMap().putLong(PARAM_NOTIFICATION_TIME, System.currentTimeMillis());
        PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                callback.run();
            }
        });
    }
}
