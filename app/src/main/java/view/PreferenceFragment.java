package view;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.scancreateprofessor.R;
import com.google.common.collect.ImmutableMap;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

        if (darkModeSwitch == null) {
            Log.e(TAG, "darkModeSwitch is null");
            throw new RuntimeException("darkModeSwitch is null");
        }
        darkModeSwitch.setDefaultValue(PreferenceManager.getInstance().getPreferenceIsDarkMode());
        darkModeSwitch.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    boolean isDarkMode = (boolean) newValue;
                    if (isDarkMode) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    PreferenceManager.getInstance().setPreferenceIsDarkMode(isDarkMode);
                    return true;
                }
        );

        // * Recognize
        // ** ink language tag
        ListPreference inkLanguageTagList = findPreference("ink_language_tag");
        if (inkLanguageTagList == null) {
            Log.e(TAG, "inkLanguageTagList is null");
            throw new RuntimeException("inkLanguageTagList is null");
        }
        List<String> inkEntries = new ArrayList<>();
        List<String> inkEntriesValues = new ArrayList<>();

        for (DigitalInkRecognitionModelIdentifier modelIdentifier :
                DigitalInkRecognitionModelIdentifier.allModelIdentifiers()
        ) {
            if (NON_TEXT_MODELS.containsKey(modelIdentifier.getLanguageTag())) {
                continue;
            }
            if (modelIdentifier.getLanguageTag().endsWith(GESTURE_EXTENSION)) {
                continue;
            }

            inkEntries.add(
                    new Locale(modelIdentifier.getLanguageSubtag()).getDisplayName() +
                            "(" +
                            modelIdentifier.getLanguageTag() +
                            ")"
            );
            inkEntriesValues.add(modelIdentifier.getLanguageTag());

        }

        inkLanguageTagList.setEntries(inkEntries.toArray(new String[0]));
        inkLanguageTagList.setEntryValues(inkEntriesValues.toArray(new String[0]));

        inkLanguageTagList.setOnPreferenceChangeListener(
                (preference, newValue) -> {
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
        );

        // ** Scanner language tag
        ListPreference scannerLanguageTagList = findPreference("scanner_language_tag");
        if (scannerLanguageTagList == null) {
            Log.e(TAG, "ScannerLanguageTagList is null");
            throw new RuntimeException("ScannerLanguageTagList is null");
        }
        List<String> scannerEntries = new ArrayList<>(
                Arrays.asList(getResources().getStringArray(R.array.scanner_model_list))
        );
        List<String> scannerEntryValue = new ArrayList<>(
                Arrays.asList(getResources().getStringArray(R.array.scanner_model_list_value))
        );
        scannerLanguageTagList.setEntries(scannerEntries.toArray(new String[0]));
        scannerLanguageTagList.setEntryValues(scannerEntryValue.toArray(new String[0]));

        scannerLanguageTagList.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    String languageTag = (String) newValue;
                    if (languageTag == null) {
                        return false;
                    }
                    Log.i(TAG, "Selected language: " + languageTag);

                    PreferenceManager.getInstance().setPreferenceTextRecognizerModel(languageTag);

                    return true;
                }
        );
    }

}