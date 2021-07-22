package ru.bruimafia.donotforget;

import android.app.Application;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;

import com.google.android.gms.ads.MobileAds;
import com.onesignal.OneSignal;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import ru.bruimafia.donotforget.repository.local_store.LocalDatabase;
import ru.bruimafia.donotforget.util.SharedPreferencesManager;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class App extends Application {

    public static App instance;
    private LocalDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        checkCurrentTheme();
        instance = this;
        database = Room.databaseBuilder(this, LocalDatabase.class, "database").build();

        // Google Mobile Ads
        MobileAds.initialize(this);

        // AppMetrica
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder(getString(R.string.appMetrica_api_key)).build();
        YandexMetrica.activate(getApplicationContext(), config);
        YandexMetrica.enableActivityAutoTracking(this);

        // OneSignal
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.oneSignal_app_id));
    }

    public static App getInstance() {
        return instance;
    }

    public LocalDatabase getDatabase() {
        return database;
    }

    public void checkCurrentTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (SharedPreferencesManager.getInstance(this).isLightTheme())
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
            else
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
        }
    }
}
