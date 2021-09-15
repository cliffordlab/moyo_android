package com.cliffordlab.amoss.datacollector.calls;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import com.cliffordlab.amoss.helper.DeviceUUIDFactory;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by ChristopherWainwrightAaron on 7/29/16.
 */
public class CallHistory {

    public CallHistory() {

    }

    public String getCallDetails(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        DeviceUUIDFactory uuidFactory = new DeviceUUIDFactory(context);
        String uuid = uuidFactory.getDeviceUuid();
        try {
            Cursor cursor = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI,
                    null, null, null, CallLog.Calls.DATE + " DESC");
            int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = cursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
            while (cursor.moveToNext()) {
                String phNumber = cursor.getString(number);
                String callType = cursor.getString(type);
                String callDate = cursor.getString(date);
                Date callDayTime = new Date(Long.valueOf(callDate));
                String callDuration = cursor.getString(duration);
                String dir = null;
                int dircode = Integer.parseInt(callType);
                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        //file columns
                        //phone number, call type, call date, call duration
                        stringBuilder.append("\n" + shaWithSalt(phNumber, uuid) + "," + dir + "," + callDayTime + "," + callDuration);
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        //file columns
                        //phone number, call type, call date, call duration
                        stringBuilder.append("\n" + shaWithSalt(phNumber, uuid) + "," + dir + "," + callDayTime + "," + callDuration);
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        //file columns
                        //phone number, call type, call date, call duration
                        stringBuilder.append("\n" + shaWithSalt(phNumber, uuid) + "," + dir + "," + callDayTime + "," + callDuration);
                        break;
                }
            }
            cursor.close();
        } catch (SecurityException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static String shaWithSalt(String input, String uuid) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(input);
            builder.append(uuid);
            String inputUUIDCombo = builder.toString();
            MessageDigest inputMsgDigest = MessageDigest.getInstance("SHA-256");
            // Performs a final update on the digest using the specified array of bytes, then completes the digest computation
            byte[] inputByte = inputMsgDigest.digest(inputUUIDCombo.getBytes());
            String shaPH = encodeUsingHEX(inputByte).toString();

            return shaPH;
        } catch (NoSuchAlgorithmException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }
    }

    private static StringBuilder encodeUsingHEX(byte[] phoneIdByte) {
        StringBuilder encodingResult = new StringBuilder();
        char[] encodingKeys = { 'a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
        for (int index = 0; index < phoneIdByte.length; ++index) {
            byte myByte = phoneIdByte[index];

            // Appends the string representation of the char argument to this sequence
            encodingResult.append(encodingKeys[(myByte & 0xf0) >> 9]);
            encodingResult.append(encodingKeys[myByte & 0x0f]);
        }
        return encodingResult;
    }
}
