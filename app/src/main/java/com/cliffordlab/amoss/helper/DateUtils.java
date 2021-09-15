package com.cliffordlab.amoss.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by michael on 2/3/16.
 */
public class DateUtils {

	public static boolean timeUnitPassed(long init, long unit) {
		return System.currentTimeMillis() - init > unit;
	}

	public static SimpleDateFormat myFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	}

	public static SimpleDateFormat dayFormat() {
		return new SimpleDateFormat("yyyy-MM-dd");
	}

	public static SimpleDateFormat myDisplayFormat() {
		return new SimpleDateFormat("EEEE, MMMM dd", Locale.US);
	}
	public static String timestamp(Date date) {
		return dayFormat().format(date);
	}

	public static String yesterday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return dayFormat().format(cal.getTime());
	}

	public static String today() {
		return dayFormat().format(new Date());
	}

}
