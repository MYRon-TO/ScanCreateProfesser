package model.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.DocumentsContract;

import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <h1> PreferenceManager </h1>
 * <p>
 *     This class is a singleton class that is used to manage shared preference.
 * </p>
 *
 * <h2> </h2>
 */
public class PreferenceManager {

    /** <h2> sharePreference </h2>
     * <p> This is the shared preference that is used to store the data. </p>
     */
    private final SharedPreferences sharedPreferences;

    /** <h2> lock </h2>
     *  <p> This is the lock that is used to lock the shared preference. </p>
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // instance
    private static final String SHARED_PREFERENCE_NAME = "scp_preference";
    private static volatile PreferenceManager instance;

    /**
     * <h2> PreferenceManager </h2>
     * You should <b>Init it once</b>
     * @see PreferenceManager#init(Context context)
     */
    public static PreferenceManager getInstance() {
        return instance;
    }

    public static PreferenceManager init(Context context) {
        if (instance != null) {
            throw new IllegalStateException("PreferenceManager is not initialized");
        }else{
            synchronized (PreferenceManager.class) {
                instance = new PreferenceManager(context);
            }
        }
        return instance;
    }

    /**
     * @see PreferenceManager#init(Context context)
     * @param context The context of the application.
     */
    private PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    // * The Example of the usage of the shared preference.
    public void setConfig_UnitTest(String key, String value) {
        lock.writeLock().lock();
        try {
            sharedPreferences.edit().putString(key, value).apply();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getConfig_UnitTest(String key) {
        lock.readLock().lock();
        try {
            return sharedPreferences.getString(key, null);
        } finally {
            lock.readLock().unlock();
        }
    }


    // TODO: 5/24/24 Added more preference

    /**
     * get the preference of the dark mode.
     * <strong> remember to check the null safe </strong>
     * @return the preference of the dark mode, <strong>true</strong> if the dark mode is enabled, <strong>false</strong> otherwise.
     */
    public boolean getPreferenceIsDarkMode() {
        lock.readLock().lock();
        try {
            return sharedPreferences.getBoolean("isDarkMode", false);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * set the preference of the dark mode.
     * <strong> remember to check the null safe </strong>
     * @param isDarkMode the preference of the dark mode, <strong>true</strong> to set the dark mode, <strong>false</strong> otherwise.
     * @return <strong>true</strong> if the preference is set successfully, <strong>false</strong> otherwise.
     */
    public boolean setPreferenceIsDarkMode(boolean isDarkMode) {
        lock.writeLock().lock();
        try {
            return sharedPreferences.edit().putBoolean("isDarkMode", isDarkMode).commit();
        } catch (Exception e) {
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * set the preference of the path to save the note.
     * <strong> remember to check the null safe </strong>
     * @return the preference of the path to save the note.
     */
    public Uri getPreferencePathToSaveNote(){
        lock.readLock().lock();
        try {
            String path = sharedPreferences.getString("pathToSaveNote", null);
            return path == null ? DocumentsContract.buildRootsUri("com.android.externalstorage.documents") : Uri.parse(path);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * set the preference of the path to save the note.
     * <strong> remember to check the null safe </strong>
     * @param path the path to save the note.
     * @return <strong>true</strong> if the preference is set successfully, <strong>false</strong> otherwise.
     */
    public boolean setPreferencePathToSaveNote(Uri path) {
        lock.writeLock().lock();
        try {
            return sharedPreferences.edit().putString("pathToSaveNote", path.toString()).commit();
        } catch (Exception e) {
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean getPreferenceIsFirstTime() {
        lock.readLock().lock();
        try {
            return sharedPreferences.getBoolean("isFirstTime", true);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setPreferenceIsFirstTime(boolean isFirstTime) {
        lock.writeLock().lock();
        try {
            sharedPreferences.edit().putBoolean("isFirstTime", isFirstTime).apply();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getPreferenceDigitalInkRecognitionModel() {
        lock.readLock().lock();
        try {
            return sharedPreferences.getString("digitalInkRecognitionModel", "en-US");
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setPreferenceDigitalInkRecognitionModel(String languageTag) {
        lock.writeLock().lock();
        try {
            sharedPreferences.edit().putString("digitalInkRecognitionModel", languageTag).apply();
        } finally {
            lock.writeLock().unlock();
        }
    }

}
