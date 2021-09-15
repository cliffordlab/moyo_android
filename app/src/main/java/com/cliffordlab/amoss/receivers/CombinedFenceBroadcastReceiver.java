package com.cliffordlab.amoss.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;

/**
 * Created by ChristopherWainwrightAaron on 1/13/17.
 */

public class CombinedFenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "Awareness";

    @Override
    public void onReceive(Context context, Intent intent) {
        FenceState fenceState = FenceState.extract(intent);

        Log.d(TAG, "Fence Receiver Received");

        if (TextUtils.equals(fenceState.getFenceKey(), "combinedFenceKey")) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.i(TAG, "Fence > User is moving.");
                    break;
                case FenceState.FALSE:
                    Log.i(TAG, "Fence > User is not moving.");
                    break;
                case FenceState.UNKNOWN:
                    Log.i(TAG, "Fence > The user's activity is at an unknown state.");
                    break;
            }
        }
    }

}
