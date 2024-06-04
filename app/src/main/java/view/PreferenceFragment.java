package view;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.scancreateprofessor.R;
import com.google.common.collect.ImmutableMap;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;

import java.util.ArrayList;
import java.util.List;

import model.preference.PreferenceManager;
import presenter.digitalink.StrokeManager;

public class PreferenceFragment extends PreferenceFragmentCompat {

    private static final String TAG = "PreferenceFragment";
    private static final ImmutableMap<String, String> NON_TEXT_MODELS =
            ImmutableMap.of(
                    "zxx-Zsym-x-autodraw",
                    "Autodraw",
                    "zxx-Zsye-x-emoji",
                    "Emoji",
                    "zxx-Zsym-x-shapes",
                    "Shapes");

    private static final String GESTURE_EXTENSION = "-x-gesture";

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preference, rootKey);
        // * Theme
        SwitchPreferenceCompat darkModeSwitch = findPreference("set_dark_mode");
        PreferenceManager.getInstance().getPreferenceIsDarkMode();

        if (darkModeSwitch == null){
            Log.e(TAG, "darkModeSwitch is null");
            throw new RuntimeException("darkModeSwitch is null");
        }
        darkModeSwitch.setDefaultValue(PreferenceManager.getInstance().getPreferenceIsDarkMode());
        darkModeSwitch.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    boolean isDarkMode = (boolean) newValue;
                    if(isDarkMode){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    PreferenceManager.getInstance().setPreferenceIsDarkMode(isDarkMode);
                    return true;
                }
        );

        // * Recognize
        ListPreference languageTagList = findPreference("language_tag");
        if (languageTagList == null) {
            Log.e(TAG, "languageTagList is null");
            throw new RuntimeException("languageTagList is null");
        }
        List<String> entries = new ArrayList<>();

        for (DigitalInkRecognitionModelIdentifier modelIdentifier :
                DigitalInkRecognitionModelIdentifier.allModelIdentifiers()) {
            if (NON_TEXT_MODELS.containsKey(modelIdentifier.getLanguageTag())) {
                continue;
            }
            if (modelIdentifier.getLanguageTag().endsWith(GESTURE_EXTENSION)) {
                continue;
            }

            entries.add(modelIdentifier.getLanguageTag());
        }

        languageTagList.setEntries(entries.toArray(new String[0]));
        languageTagList.setEntryValues(entries.toArray(new String[0]));

        languageTagList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                String languageTag = (String) newValue;
                if (languageTag == null) {
                    return false;
                }
                Log.i(TAG, "Selected language: " + languageTag);

                PreferenceManager.getInstance().setPreferenceDigitalInkRecognitionModel(languageTag);
                StrokeManager.getInstance().setActiveModel(languageTag);

                StrokeManager.getInstance().downloadModel();

                return true;
            }
        });
    }

}