package com.cliffordlab.amoss.datacollector.liwc;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.cliffordlab.amoss.helper.Constants.LIWC_LOG_FILENAME;

/**
 * Created by ChristopherWainwrightAaron on 6/15/16.
 */
public class LiwcJobLogger {

    private final Context mContext;
    private final String filename = LIWC_LOG_FILENAME;

    public LiwcJobLogger(Context context) {
        mContext = context;
    }

    protected boolean writeToLogger() {
        try {
            long timeMillis = System.currentTimeMillis();
            String content = timeMillis + "\n";
            //Specify the file name and path here
            File file = new File(mContext.getFilesDir(), filename); // access apps internal file directory

            if(!file.exists()) {
                file.createNewFile();
            }

            RandomAccessFile f = new RandomAccessFile(file, "rw");
            f.seek(0); // to the beginning
            f.write(content.getBytes());
            f.close();

            System.out.println("Data successfully appended at the end of file");
            return true;
        } catch(IOException ioe) {
            FirebaseCrashlytics.getInstance().recordException(ioe);
            System.out.println("Exception occurred:");
            ioe.printStackTrace();
            return false;
        }
    }

    protected String readLoggerForTime() {
        BufferedReader br = null;
        String timestamp = null;

        try {
            File file = new File(mContext.getFilesDir(), filename);

            if(!file.exists()) {
                file.createNewFile();
            }

            br = new BufferedReader(new FileReader(file));

            timestamp = br.readLine();

        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                FirebaseCrashlytics.getInstance().recordException(ex);
                ex.printStackTrace();
            }
        }
        return timestamp;
    }
}
