package presenter;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        if (context == null) {
            Log.e("MainApplication", "Context is null");
        }

        NoteManager.init(getApplicationContext());
    }

}
