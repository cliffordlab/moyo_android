package com.cliffordlab.amoss.datacollector.liwc;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import com.cliffordlab.amoss.helper.CSVCreator;
import com.cliffordlab.amoss.helper.DeviceUUIDFactory;
import com.evernote.android.job.Job;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ChristopherWainwrightAaron on 6/9/16.
 */
//TODO change to rxjava from async task
public class LiwcParserJob extends Job {
    public static final String JOB_TAG = "LIWC_PARSER_JOB";
    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        getTextDataObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse, this::handleError);
        return Result.SUCCESS;
    }

    private void handleError(Throwable e) {
        System.out.print("Error for parser job");
        System.out.println(e.getMessage());
    }

    private void handleResponse(Boolean success) {
        if (success) {
            System.out.println("Parser Job Completed");
        } else {
            System.out.println("Parser Job did not Complete");
        }
    }

    private Observable<Boolean> getTextDataObservable() {
        return Observable.fromCallable(() -> {
            LiwcParserTask task = new LiwcParserTask();
            return task.writeTextData(getContext());
        });
    }

    private class LiwcParserTask {
        private final StringBuilder mStringBuilder = new StringBuilder();
        private final List<String> phArray = new ArrayList<>();
        private final List<String> callTypeArray = new ArrayList<>();

        boolean writeTextData(Context context) {
            LiwcJobLogger logger = new LiwcJobLogger(context);
            String time = logger.readLoggerForTime(); // checking for most recent time in logger
            LiwcParser parser = new LiwcParser(getContext());
            LongSparseArray<String> messages = getMessages(context); // using sparse array with all messages in inbox
            if (time == null) { // if time is null means it is first time checking inbox and will run parser on all messages
                for (int i = 0; i < messages.size(); i++) {
                    HashMap<String, Integer> tempMap = parser.parseMessage(messages.valueAt(i));
                    for (String key : tempMap.keySet()) {
                        mStringBuilder.append(key);
                        mStringBuilder.append("=");
                        mStringBuilder.append(tempMap.get(key));
                        mStringBuilder.append(",");
                    }
                    mStringBuilder.append("length=");
                    mStringBuilder.append(messages.valueAt(i).length());
                    mStringBuilder.append(",");
                    mStringBuilder.append("time=");
                    mStringBuilder.append(messages.keyAt(i));
                    mStringBuilder.append(",");
                    mStringBuilder.append("phNumber=");
                    mStringBuilder.append(phArray.get(i));
                    mStringBuilder.append(",");
                    mStringBuilder.append("type=");
                    mStringBuilder.append(callTypeArray.get(i));
                    mStringBuilder.append("\n");
                }
            } else { // will only run parser on messages made after job ran last
                long longTime = Long.parseLong(time);
                for (int i = 0; i < messages.size(); i++) {
                    if (longTime < messages.keyAt(i)){
                        HashMap<String, Integer> tempMap = parser.parseMessage(messages.valueAt(i));
                        for (String key : tempMap.keySet()) {
                            mStringBuilder.append(key);
                            mStringBuilder.append("=");
                            mStringBuilder.append(tempMap.get(key));
                            mStringBuilder.append(",");
                        }
                        mStringBuilder.append("length=");
                        mStringBuilder.append(messages.valueAt(i).length());
                        mStringBuilder.append(",");
                        mStringBuilder.append("time=");
                        mStringBuilder.append(messages.keyAt(i));
                        mStringBuilder.append(",");
                        mStringBuilder.append("phNumber=");
                        mStringBuilder.append(phArray.get(i));
                        mStringBuilder.append(",");
                        mStringBuilder.append("type=");
                        mStringBuilder.append(callTypeArray.get(i));
                        mStringBuilder.append("\n");
                    }
                }
            }
            CSVCreator csvCreator = new CSVCreator(getContext());
            String fileName = csvCreator.getFileName(System.currentTimeMillis(), "sms", ".csv");

            File file = new File(getContext().getFilesDir() + "/amoss");
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(getContext().getFilesDir() + "/amoss/" + fileName);

            FileWriter fw = null;
            BufferedWriter bw = null;

            try{
                if(!file.exists()) {
                    file.createNewFile();
                }
                fw = new FileWriter(file);
                bw = new BufferedWriter(fw);
                bw.write(mStringBuilder.toString());
                bw.close();
                fw.close();
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }

            return logger.writeToLogger(); // writes time job ended to log file
        }

        private LongSparseArray<String> getMessages(Context context) {
            DeviceUUIDFactory mUUIDFactory = new DeviceUUIDFactory(context);
            LongSparseArray<String> sms = new LongSparseArray<>();// creating a list of key value pairs
            // other option is Uri.parse("content://sms/inbox");
            ContentResolver cr = context.getContentResolver();
            Cursor cur = cr.query(Uri.parse("content://sms/"),null,null,null,null); // using cursor to iterate over inbox
            /*
            message types:
                0 : type all
                1 : type inbox
                2 : type sent
                3 : type draft
                4 : type outbox
                5 : type failed
                6 : type queued
             */
            if (cur != null) {
                while (cur.moveToNext()) {
                    String date = cur.getString(cur.getColumnIndex("date"));
                    String body = cur.getString(cur.getColumnIndex("body"));
                    String phNumber = cur.getString(cur.getColumnIndex("address"));
                    String type = cur.getString(cur.getColumnIndex("type"));
                    phArray.add(shaWithSalt(phNumber, mUUIDFactory.getDeviceUuid()));
                    callTypeArray.add(type);
                    Long timestamp = Long.parseLong(date);
                    sms.append(timestamp,body);
                }
            }
            cur.close();
            Log.i(JOB_TAG, "getMessages: " + sms);
            return sms;
        }

//        public BarDataSet getMessagesForGraph(Context context) {
//            List<Integer> textList = new ArrayList<>();
//            List<BarEntry> entires = new ArrayList<>();
//
//            String currentDate;
//            DeviceUUIDFactory mUUIDFactory = new DeviceUUIDFactory(context);
//            LongSparseArray<String> sms = new LongSparseArray<>();// creating a list of key value pairs
//            // other option is Uri.parse("content://sms/inbox");
//            ContentResolver cr = context.getContentResolver();
//            String selectQuery =
//                    "SELECT COUNT(*)" +
//                            " FROM " + "content://sms/" +
//                            " WHERE " + "date" + " = 'COLUMN_VALUE'";
//            Cursor cur = cr.query(Uri.parse("content://sms/"),null,null,null,null); // using cursor to iterate over inbox
//
//            /*
//            message types:
//                0 : type all
//                1 : type inbox
//                2 : type sent
//                3 : type draft
//                4 : type outbox
//                5 : type failed
//                6 : type queued
//             */
//            if (cur != null) {
//                while (cur.moveToNext()) {
//                    String date = cur.getString(cur.getColumnIndex("date"));
//                    cur.getColumnIndex("date");
//                    String type = cur.getString(cur.getColumnIndex("type"));
//                    callTypeArray.add(type);
//                    Long timestamp = Long.parseLong(date);
//                    sms.append(timestamp,body);
//                }
//            }
//            cur.close();
//            Log.i(JOB_TAG, "getMessages: " + sms);
//            return set;
//        }

        //TODO repeating code from other class need to refactor
        private String shaWithSalt(String input, String uuid) { // will need to change this to SHA2
            try {
                StringBuilder builder = new StringBuilder();
                builder.append(input);
                builder.append(uuid);
                String inputUUIDCombo = builder.toString();
                MessageDigest inputMsgDigest = MessageDigest.getInstance("SHA-256");
                // Performs a final update on the digest using the specified array of bytes, then completes the digest computation
                byte[] inputByte = inputMsgDigest.digest(inputUUIDCombo.getBytes());

                return encodeUsingHEX(inputByte).toString();
            } catch (NoSuchAlgorithmException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                return null;
            }
        }

        private StringBuilder encodeUsingHEX(byte[] phoneIdByte) {
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

}