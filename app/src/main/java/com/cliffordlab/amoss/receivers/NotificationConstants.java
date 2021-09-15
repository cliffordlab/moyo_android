package com.cliffordlab.amoss.receivers;

/**
 * Created by ChristopherWainwrightAaron on 5/11/17.
 */

public class NotificationConstants {

    private NotificationConstants() {}

    public static final String DISMISSAL = "dismissal";
    public static final String DISMISSED_MOOD_ZOOM = "dismissedMoodZoom";
    public static final String DISMISSED_MOOD_SWIPE = "dismissedMoodSwipe";
    public static final String DISMISSED_KCCQ = "dismissedKCCQ";
    public static final String DISMISSED_PHQ9 = "dismissedPhq9";
    public static final String DISMISSED_PTSD = "dismissedPTSD";
    public static final String DISMISSED_ALL_PCRF = "dismissedAllpCRF";
    public static final String DISMISSED_WEEKLY = "dismissedAllWEEKLY";
    public static final String DISMISSED_DAILY = "dismissedAllDAILY";
    public static final int REQUEST_CODE_MOODZOOM = 0;
    public static final int REQUEST_CODE_MOODSWIPE = 1;
    public static final int REQUEST_CODE_PHQ9 = 2;
    public static final int REQUEST_CODE_KCCQ = 3;
    public static final int REQUEST_CODE_WEEKLY = 4;
    public static final int REQUEST_CODE_DAILY = 5;
}
