package com.cliffordlab.amoss.datacollector.liwc;

import android.content.Context;

import com.cliffordlab.amoss.R;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ChristopherWainwrightAaron on 6/9/16.
 */

public class LiwcParser {
    private final Context mContext;
    private final HashMap<String,Integer> categoryMap;
    private final HashMap<Integer,String> categoryRowIndexMap;
    private final List<String[]> twoDArray;

    public LiwcParser(Context context) {
        mContext = context;
        categoryMap = new HashMap<>();
        categoryRowIndexMap = new HashMap<>();
        twoDArray = convertLiwcCsvToArrayofArrays();
        setUpArraysAndHashMaps();
    }

    private void setUpArraysAndHashMaps() {
        //remove top 3 rows and get header with is third row
        twoDArray.remove(0);
        twoDArray.remove(0);
        String[] categories = twoDArray.remove(0);

        for (int i = 0;i < categories.length; i++) {
            //categoryMap will contain the categories
            //and how many times a word in a message from
            //that category appeared in that message
            categoryMap.put(categories[i],0);
            //will use to check what category a word
            //is in if a match is found
            categoryRowIndexMap.put(i,categories[i]);
        }
    }

    private List<String[]> convertLiwcCsvToArrayofArrays() {
        StringBuilder builtData = new StringBuilder();
        try {
            //appending data from file to string builder
            BufferedReader br = new BufferedReader(new InputStreamReader(mContext.getResources().openRawResource(R.raw.liwc2007dictionary_1col_format)));
            String line;
            while((line = br.readLine()) != null) {
                builtData.append(line);
                builtData.append('\n');
            }
            br.close();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }

        // data that will be split in a list
        String data = builtData.toString();
        //creating new value in array at every new line
        String[] dataArray = data.split("\n");

        //creating two d array by turning each row into
        //an array and appending it to a dynamic array
        List<String[]> stringData = new ArrayList<>();
        for (String line: dataArray) {
            String[] rowArray = line.split(",");
            stringData.add(rowArray); // should end up looking like [["xxxxx"],["xxxxxx"]] etc
        }
        return stringData;
    }

    public HashMap<String,Integer> parseMessage(String message) {
        //replacing n/a so that does not get used from the csv
        String newMessage = message.replace("n/a","");
        //at every space which denotes a new word split string for array
        String[] messageArray = newMessage.split(" ");
        HashMap<String,Integer> messageMap = new HashMap<>();
        for(int i = 0;i < messageArray.length;i++) {
            if(messageMap.containsKey(messageArray[i])) {
                String key = messageArray[i];
                int value = messageMap.get(key) + 1;
                messageMap.put(key,value);
            } else {
                messageMap.put(messageArray[i],1);
            }
        }

        for(int i = 0;i<twoDArray.size();i++) {
            String[] row = twoDArray.get(i);
            for(int j = 0;j<row.length;j++) {
                //distinguishing between matching exactly and containing word
                if(row[j].contains("*")) {
                    //root variable is the word without the asterisk
                    String root = row[j].substring(0,row[j].length() - 1);
                    String[] distinctWordsInMessage = messageMap.keySet().toArray(new String[]{});
                    //loop through find the words that contain the root
                    //do not need to worry about smaller roots repeating
                    //because liwc data has accounted for that and only
                    //gave asterisk to long enough and unique roots
                    for(int k = 0;k<distinctWordsInMessage.length;k++) {
                        if(distinctWordsInMessage[k].startsWith(root)){
                            //check category of word by the index associated with it
                            String category = categoryRowIndexMap.get(j);
                            //add the value for that key in message map to category map value
                            int newValueForCategory = categoryMap.get(category) + messageMap.get(distinctWordsInMessage[k]);
                            categoryMap.put(category,newValueForCategory);
                        }
                    }
                } else {
                    //if message has word in this row
                    if(messageMap.containsKey(row[j])){
                        //check category of word by the index associated with it
                        String category = categoryRowIndexMap.get(j);
                        //add the value for that key in message map to category map value
                        int newValueForCategory = categoryMap.get(category) + messageMap.get(row[j]);
                        categoryMap.put(category,newValueForCategory);
                    }
                }
            }
        }
        return categoryMap;
    }
}
