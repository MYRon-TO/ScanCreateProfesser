package presenter;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import model.preference.PreferenceManager;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        if (context == null) {
            Log.e("MainApplication", "Context is null");
        }

        PreferenceManager.init(context);
        NoteManager.init(context);

        boolean isDarkMode = PreferenceManager.getInstance().getPreferenceIsDarkMode();
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}
