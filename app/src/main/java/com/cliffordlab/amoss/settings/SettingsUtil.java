package com.cliffordlab.amoss.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by michael on 2/1/16.
 */
public class SettingsUtil {
	private static final String TOKEN = "token";
	private static final String PARTICIPANT_ID = "participant_id";

	private static final String STUDY_ID = "study_id";
	public static final String NO_TOKEN = "no_token";
	private static final String DATA = "data";
	private static final String PHYS_ACT_DATA = "accel_data";
	private static final String LOC_DATA = "location_data";
	private static final String PHQ9 = "phq9";
	private static final String LAST_VERSION = "LAST_VERSION";
	private static final String LOGIN = "login";
	private static final String GOOGLEFIT = "googleFit";
	private static final String TIMEZONE = "timezone";
	private static final String SURVEYSCHEDULE = "survey_schedule";
	private static final String EMAIL = "email";
	private static final String FHIR = "fhir_code";
	private static final int VERSION_BUILD = 13; // Same as the version build in build.gradle
	private static final String EPIC_TOKEN = "fhir_token";
	private static final String PATIENT_ID = "patient_id";
	private static final String EPIC_TOKEN_CREATION_TIME = "epic_token_creation_time";
	private static final String IS_LOGGED_IN_AMOSS = "is_logged_in_amoss";
	private static final String IS_PRIMARY_DEVICE = "is_primary_device";
	private static final String PCL5 = "pcl5";
	private static final String HF_ASLEEP = "hf_asleep";
	private static final String HF_AWAKE = "hf_awake";
	private static final String SUDS = "suds";
	private final Context mContext;
	private final SharedPreferences prefs;

	public SettingsUtil(Context context) {
		this.mContext = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public static void setPrimaryDeviceForUser(final Context context, boolean bool) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putBoolean(IS_PRIMARY_DEVICE, bool).apply();
	}

	public static boolean isPrimaryUserDevice(final Context context) {
		return PreferenceManager
				.getDefaultSharedPreferences(context)
				.getBoolean(IS_PRIMARY_DEVICE, true);
	}

	public void isLoggedInViaAmoss(boolean b) {
		prefs.edit().putBoolean(IS_LOGGED_IN_AMOSS, b).apply();
	}

	public boolean isAmossLoggedIn() {
		return prefs.getBoolean(IS_LOGGED_IN_AMOSS, false);
	}

	public void setEpicTokenCreationTime(long epicTokenCreationTime) {
		prefs.edit().putLong(EPIC_TOKEN_CREATION_TIME, epicTokenCreationTime).apply();
	}

	public long getEpicTokenCreationTime() {
		return prefs.getLong(EPIC_TOKEN_CREATION_TIME, 0);
	}

	public void	addStudyId(String studyId) {
		prefs.edit().putString(STUDY_ID, studyId).apply();
	}

	public void	setPatientId(String patientId) {
		prefs.edit().putString(PATIENT_ID, patientId).apply();
	}

	public void	setEpicToken(String epicToken) {
		prefs.edit().putString(EPIC_TOKEN, epicToken).apply();
	}

	public String getEpicToken() {
		return prefs.getString(EPIC_TOKEN, "");
	}


	public String getPatientID() {
		return prefs.getString(PATIENT_ID, "");
	}

	public String getStudyId() {
		return prefs.getString(STUDY_ID, "");
	}

	public void setEmail(String email) {
		prefs.edit().putString(EMAIL, email).apply();
	}

	public String getEmail() {
		return prefs.getString(EMAIL, "no email");
	}

	public void setFhirCode(String code) { prefs.edit().putString(FHIR, code).apply(); }

	public String getFhirCode() { return  prefs.getString(FHIR, "no fhir code"); }

	public boolean hasSetSchedule() {
		return prefs.getBoolean(SURVEYSCHEDULE, false);
	}

	public void setHasSetSchedule(boolean surveysSet) {
		prefs.edit().putBoolean(SURVEYSCHEDULE, surveysSet).apply();
	}



	public void setHasCompletedZoom(boolean complete) {
		prefs.edit().putBoolean("zoom", complete).apply();
	}

	public boolean hasCompletedZoom() {
		return prefs.getBoolean("zoom", false);
	}



	public void setHasCompletedSwipe(boolean complete) {
		prefs.edit().putBoolean("swipe", complete).apply();
	}

	public boolean hasCompletedSwipe() {
		return prefs.getBoolean("swipe", false);
	}

	public void setHasCompletedKCCQ(boolean complete) {
		prefs.edit().putBoolean("kccq", complete).apply();
	}

	public boolean hasCompletedKCCQ() {
		return prefs.getBoolean("kccq", false);
	}

	public void setHasCompletedPHQ9(boolean complete) {
		prefs.edit().putBoolean("phq9", complete).apply();
	}

	public boolean hasCompletedPHQ9() {
		return prefs.getBoolean("phq9", false);
	}

	public boolean isAuthorizedToLogin() {
		return prefs.getBoolean(LOGIN, true);
	}

	public void setIsAuthorizedToLogin(boolean authorizedToLogin) {
		prefs.edit().putBoolean(LOGIN, authorizedToLogin).apply();
	}

	public String timezone() {
		return prefs.getString(TIMEZONE, null);
	}

	public void setNewTimezone(String timezone) {
		prefs.edit().putString(TIMEZONE, timezone).apply();
	}

	public boolean hasGoogleFitClient() {
		return prefs.getBoolean(GOOGLEFIT, false);
	}

	public void setHasGoogleFitClient() {
		prefs.edit().putBoolean(GOOGLEFIT, true).apply();
	}

	public static void setDataCollection(final Context context, boolean shouldCollectData) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putBoolean(DATA, shouldCollectData).apply();
	}

	public static boolean isDataCollected(final Context context) {
		return PreferenceManager
						.getDefaultSharedPreferences(context)
						.getBoolean(DATA, true);
	}

	//used by settings activity class to figure out
	public void setAccCollection(boolean collecting) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		prefs.edit().putBoolean(PHYS_ACT_DATA, collecting).apply();
	}

	public void setLocCollection(boolean collecting) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit().putBoolean(LOC_DATA, collecting).apply();
    }

	public void setGoogleDataCollection(boolean collecting) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		prefs.edit().putBoolean(CollectionSettings.GOOGLE.toString(), collecting).apply();
	}

	public boolean isAccCollectedEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(PHYS_ACT_DATA, false);
	}

	public boolean isLocCollectionEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(LOC_DATA, false);
    }

	public boolean isSocialDataCollectionEnabled() {
		return isCallDataCollectionEnabled() || isLIWCDataCollectionEnabled();
	}

	public boolean isCallDataCollectionEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(CollectionSettings.SOCIAL.toString(), false);
	}

	public boolean isLIWCDataCollectionEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(CollectionSettings.SOCIAL.toString(), false);
	}

	public boolean isGoogleDataCollectionEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(CollectionSettings.GOOGLE.toString(), false);
	}

	public static boolean isAuthenticated(final Context context) {
		String token = PreferenceManager.getDefaultSharedPreferences(context).getString(TOKEN, NO_TOKEN);
		return !token.equals(NO_TOKEN);
	}

	public static void addToken(final Context context, String token) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(TOKEN, token).apply();
	}

	public static long getParticipantId(final Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(PARTICIPANT_ID, -1);
	}

	public static void addParticipantID(final Context context, long id) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putLong(PARTICIPANT_ID, id).apply();
	}

	/**
	 * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
	 * @param complete For whether or not phq9 survey was completed.
	 */
	public static void setPHQ9Completed(final Context context, boolean complete) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putBoolean(PHQ9, complete).apply();
	}

	/**
	 * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
	 */
	public static boolean isPHQ9Completed(final Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PHQ9, false);
	}

	/**
	 * check if app has been updated
	 * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
	 */
	public static boolean isAppUpdated(final Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int last = prefs.getInt(LAST_VERSION, 0);
		if (last < VERSION_BUILD) {
			//Your code
			prefs.edit().putInt(LAST_VERSION, VERSION_BUILD).apply();
			return true;
		}
		return false;
	}

	public static String authToken(final Context context) {
		return PreferenceManager
						.getDefaultSharedPreferences(context)
						.getString(TOKEN, NO_TOKEN);
	}

	public static void clear(final Context context) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
	}

	/**
	 * Helper method to register a settings_prefs listener. This method does not automatically handle
	 * {@code unregisterOnSharedPreferenceChangeListener() un-registering} the listener at the end
	 * of the {@code context} lifecycle.
	 *
	 * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
	 * @param listener Listener to register.
	 */
	public static void register(final Context context,
															SharedPreferences.OnSharedPreferenceChangeListener listener) {
		PreferenceManager
						.getDefaultSharedPreferences(context)
						.registerOnSharedPreferenceChangeListener(listener);
	}

	/**
	 * Helper method to un-register a settings_prefs listener typically registered with
	 * {@code registerOnSharedPreferenceChangeListener()}
	 *
	 * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
	 * @param listener Listener to un-register.
	 */
	public static void unregister(final Context context,
																SharedPreferences.OnSharedPreferenceChangeListener listener) {
		PreferenceManager
						.getDefaultSharedPreferences(context)
						.unregisterOnSharedPreferenceChangeListener(listener);
	}

    public static void setPCL5Completed(@Nullable Context context, boolean complete) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putBoolean(PCL5, complete).apply();
    }



	public void setSleepScheduleTimeAsleep(@Nullable Context context, @NotNull String time) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(HF_ASLEEP, time).apply();
	}

	public void setSleepScheduleTimeAwake(@Nullable Context context, @NotNull String time) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putString(HF_AWAKE, time).apply();
	}

	public static void setSUDSCompleted(@Nullable Context context, boolean complete) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putBoolean(SUDS, complete).apply();
	}

	public static boolean isPCL5Completed(final Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PCL5, false);
	}

	public static boolean isSUDSCompleted(final Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SUDS, false);
	}

	public void setLIWCDataCollection(boolean collecting) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		prefs.edit().putBoolean(CollectionSettings.LIWC.toString(), collecting).apply();
	}

	public void setCallDataCollection(boolean collecting) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		prefs.edit().putBoolean(CollectionSettings.CALL.toString(), collecting).apply();
	}
}
