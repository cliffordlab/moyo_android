package com.cliffordlab.amoss.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cliffordlab.amoss.settings.SettingsUtil;

/**
 * Created by ChristopherWainwrightAaron on 7/17/17.
 */

public class LoginReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SettingsUtil util = new SettingsUtil(context);
        util.setIsAuthorizedToLogin(true);
    }
}
