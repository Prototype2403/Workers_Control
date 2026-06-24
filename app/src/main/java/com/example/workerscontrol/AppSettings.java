package com.example.workerscontrol;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class AppSettings {

    private static final String PREFS = "app_settings";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_ADMIN_NOTIFICATIONS = "admin_notifications";

    private AppSettings() {
    }

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isDarkThemeEnabled(Context context) {
        return prefs(context).getBoolean(KEY_DARK_THEME, false);
    }

    public static void setDarkThemeEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DARK_THEME, enabled).apply();
    }

    public static String getLanguageCode(Context context) {
        return prefs(context).getString(KEY_LANGUAGE, "ru");
    }

    public static void setLanguageCode(Context context, String code) {
        prefs(context).edit().putString(KEY_LANGUAGE, code).apply();
    }

    public static boolean isAdminNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ADMIN_NOTIFICATIONS, false);
    }

    public static void setAdminNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ADMIN_NOTIFICATIONS, enabled).apply();
    }

    public static boolean isWorkerNotifyIconEnabled(Context context, long workerId) {
        return prefs(context).getBoolean("worker_notify_icon_" + workerId, false);
    }

    public static void setWorkerNotifyIconEnabled(Context context, long workerId, boolean enabled) {
        prefs(context).edit().putBoolean("worker_notify_icon_" + workerId, enabled).apply();
    }

    public static String getWorkerStartReminder(Context context, long workerId) {
        return prefs(context).getString("worker_start_reminder_" + workerId, "never");
    }

    public static void setWorkerStartReminder(Context context, long workerId, String value) {
        prefs(context).edit().putString("worker_start_reminder_" + workerId, value).apply();
    }

    public static String getWorkerEndReminder(Context context, long workerId) {
        return prefs(context).getString("worker_end_reminder_" + workerId, "never");
    }

    public static void setWorkerEndReminder(Context context, long workerId, String value) {
        prefs(context).edit().putString("worker_end_reminder_" + workerId, value).apply();
    }

    public static boolean isWorkerAlarmOnLateEnabled(Context context, long workerId) {
        return prefs(context).getBoolean("worker_alarm_late_" + workerId, false);
    }

    public static void setWorkerAlarmOnLateEnabled(Context context, long workerId, boolean enabled) {
        prefs(context).edit().putBoolean("worker_alarm_late_" + workerId, enabled).apply();
    }

    public static void applyTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(
                isDarkThemeEnabled(context) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static void applyLanguage(Context context) {
        String code = getLanguageCode(context);
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code));
    }
}
