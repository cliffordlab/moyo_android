package com.cliffordlab.amoss.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.UUID;

/**
 * Created by ChristopherWainwrightAaron on 5/8/17.
 */

public class DeviceUUIDFactory {
    static final String PREFS_FILE = "device_id.xml";
    static final String PREFS_DEVICE_ID = "device_id";
    volatile static String uuid;
    protected Context mContext;

    public DeviceUUIDFactory(Context context) {
        this.mContext = context;
        if (uuid == null) {
            synchronized (DeviceUUIDFactory.class) {
                if (uuid == null) {
                    final SharedPreferences prefs = context
                            .getSharedPreferences(PREFS_FILE, 0);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        // Use the ids previously computed and stored in the
                        // prefs file
                        uuid = id;
                    } else {
                        final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        // Use the Android ID unless it's broken, in which case
                        // fallback on deviceId,
                        // unless it's not available, then fallback on a random
                        // number which we store to a prefs file

                        if (!"9774d56d682e549c".equals(androidId)) {
                            uuid = androidId;
                        } else {
                            final String deviceId = (
                                    (TelephonyManager) context
                                            .getSystemService(Context.TELEPHONY_SERVICE))
                                    .getDeviceId();
                            if (deviceId != null) {
                                uuid = deviceId;
                            } else {
                                uuid = UUID.randomUUID().toString();
                            }
                        }
                        // Write the value out to the prefs file
                        prefs.edit().putString(PREFS_DEVICE_ID, uuid).apply();
                    }
                }
            }
        }
    }

    public String getDeviceUuid() {
        return uuid;
    }

}
