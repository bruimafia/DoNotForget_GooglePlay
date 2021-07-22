package ru.bruimafia.donotforget.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String APP_PREFERENCES = "com.bruimafia.donotforget";
    private static final String IS_FULL_VERSION = "FULL_VERSION"; // версия приложения
    private static final String IS_LIGHT_THEME = "LIGHT_THEME"; // тема приложения
    private static final String IS_PLAY_RATING = "PLAY_RATING"; // оценка игры (Google play)
    private static final String IS_ORDER_BY_ID = "ORDER_BY_ID"; // стандартная сортировка
    private static final String IS_FIRST_LAUNCH = "FIRST_LAUNCH"; // первый запуск
    private static final String LAST_SYNC = "LAST_SYNC"; // последняя синхронизация

    private static SharedPreferencesManager instance;
    private final SharedPreferences sPref;

    private SharedPreferencesManager(Context context) {
        sPref = context.getApplicationContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null)
            instance = new SharedPreferencesManager(context);
        return instance;
    }

    public boolean isFullVersion() {
        return sPref.getBoolean(IS_FULL_VERSION, false);
    }

    public void setFullVersion(boolean isFullVersion) {
        sPref.edit().putBoolean(IS_FULL_VERSION, isFullVersion).apply();
    }

    public boolean isLightTheme() {
        return sPref.getBoolean(IS_LIGHT_THEME, true);
    }

    public void setLightTheme(boolean isLightTheme) {
        sPref.edit().putBoolean(IS_LIGHT_THEME, isLightTheme).apply();
    }

    public boolean isPlayRating() {
        return sPref.getBoolean(IS_PLAY_RATING, false);
    }

    public void setPlayRating(boolean isPlayRating) {
        sPref.edit().putBoolean(IS_PLAY_RATING, isPlayRating).apply();
    }

    public boolean isOrderById() {
        return sPref.getBoolean(IS_ORDER_BY_ID, true);
    }

    public void setOrderById(boolean orderById) {
        sPref.edit().putBoolean(IS_ORDER_BY_ID, orderById).apply();
    }

    public boolean isFirstLaunch() {
        return sPref.getBoolean(IS_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean firstLaunch) {
        sPref.edit().putBoolean(IS_FIRST_LAUNCH, firstLaunch).apply();
    }

    public long getLastSync() {
        return sPref.getLong(LAST_SYNC, 0);
    }

    public void setLastSync(long time) {
        sPref.edit().putLong(LAST_SYNC, time).apply();
    }

}
