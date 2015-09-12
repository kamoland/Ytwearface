package com.kamoland.ytwearface.data;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class MobileIF {
    private static final String QUERY_YTLOGSTATUS_PATH = "/com.kamoland.ytwearface.p7";

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private boolean mAutoClose;

    public MobileIF(Context context, boolean autoCloseAfterSend) {
        mContext = context;
        mAutoClose = autoCloseAfterSend;
    }

    public void queryYtlogStatus() {
        sendData(QUERY_YTLOGSTATUS_PATH, "");
    }

    public void finish() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    private void sendData(final String path, final String mes) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            doSendData(mGoogleApiClient, path, mes, new Runnable() {
                @Override
                public void run() {
                    if (mAutoClose) {
                        finish();
                    }
                }
            });
            return;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        doSendData(mGoogleApiClient, path, mes, new Runnable() {
                            @Override
                            public void run() {
                                if (mAutoClose) {
                                    finish();
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {}
                }).build();
        mGoogleApiClient.connect();
    }

    private static void doSendData(final GoogleApiClient mGoogleApiClient, final String dataPath,
                                   final String mes, final Runnable callback) {
        (new Thread() {
            @Override
            public void run() {
                byte[] data = mes.getBytes();
                NodeApi.GetConnectedNodesResult nodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node0 : nodes.getNodes()) {
                    String node = node0.getId();
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node, dataPath, data).await(5, TimeUnit.SECONDS);
                    boolean success = result.getStatus().isSuccess();
                }
                callback.run();
            }
        }).start();
    }
}
