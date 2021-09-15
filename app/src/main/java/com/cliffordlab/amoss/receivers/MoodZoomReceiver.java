package com.cliffordlab.amoss.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.cliffordlab.amoss.app.AmossApplication;
import com.cliffordlab.amoss.services.NotificationDismissalService;
import com.cliffordlab.amoss.helper.Constants;
import com.cliffordlab.amoss.gui.MainActivity;
import com.cliffordlab.amoss.R;
import java.util.Calendar;
import java.util.TimeZone;
import static com.cliffordlab.amoss.receivers.NotificationConstants.*;

/**
 * Created by michael on 2/2/16.
 */
public class MoodZoomReceiver extends BroadcastReceiver {

	private AlarmManager am;
	private PendingIntent pendingIntent;

	@Override
	public void onReceive(Context context, Intent intent) {
		int requestID = (int) System.currentTimeMillis();
		int deleteRequestID = (int) System.currentTimeMillis();

		Intent result = new Intent(context, MainActivity.class);
		result.putExtra(Constants.NOTIFICATION_FLAG, Constants.MOOD_ZOOM_NOTIFICATION_ID);
		result.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		Intent deleteResult = new Intent(context, NotificationDismissalService.class);
		deleteResult.putExtra(DISMISSAL, DISMISSED_MOOD_ZOOM);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, requestID, result, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent deleteIntent = PendingIntent.getService(context, deleteRequestID, deleteResult, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
						.setAutoCancel(true)
						.setSmallIcon(R.drawable.checklist)
						.setContentTitle("MoYo")
						.setDefaults(Notification.DEFAULT_SOUND)
						.setPriority(NotificationCompat.PRIORITY_HIGH)
						.setContentText("Time to take your daily Mood Zoom!")
						.setDeleteIntent(deleteIntent)
						.setContentIntent(pendingIntent);

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(Constants.MOOD_ZOOM_NOTIFICATION_ID, builder.build());

		//TODO quick fix but need to move to another object
		((AmossApplication)context.getApplicationContext()).scheduleAllJobs();
	}

	public void setRepeatedNotification(Context context, int id, int hh, int mm, int ss) {
		am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Intent alarm_intent = new Intent(context, MoodZoomReceiver.class);
		pendingIntent = PendingIntent.getBroadcast(context, id, alarm_intent, 0);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getDefault());
		calendar.set(Calendar.HOUR_OF_DAY, hh);
		calendar.set(Calendar.MINUTE, mm);
		calendar.set(Calendar.SECOND, ss);

		if (calendar.before(Calendar.getInstance())) {
			calendar.add(Calendar.DATE, 1);
		}

		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

//		ComponentName receiver = new ComponentName(context, StartupReceiver.class);
//		PackageManager pm = context.getPackageManager();
//
//		pm.setComponentEnabledSetting(receiver,
//						PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//						PackageManager.DONT_KILL_APP);
	}

	public void cancel(Context context) {
		if (am != null) {
			am.cancel(pendingIntent);
		}
//		ComponentName receiver = new ComponentName(context, StartupReceiver.class);
//		PackageManager pm = context.getPackageManager();
//
//		pm.setComponentEnabledSetting(receiver,
//						PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//						PackageManager.DONT_KILL_APP);
	}
}
