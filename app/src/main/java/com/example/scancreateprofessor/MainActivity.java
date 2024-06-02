package com.example.scancreateprofessor;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import presenter.digitalink.StrokeManager;
import view.DrawingView;
import view.StatusTextView;

/**
 * Main activity which creates a StrokeManager and connects it to the DrawingView.
 */
public class MainActivity extends AppCompatActivity {
    //public class MainActivity extends AppCompatActivity implements StrokeManager.DownloadedModelsChangedListener {
    private static final String TAG = "Activity";
    //    private static final String GESTURE_EXTENSION = "-x-gesture";
//    private static final ImmutableMap<String, String> NON_TEXT_MODELS =
//            ImmutableMap.of(
//                    "zxx-Zsym-x-autodraw",
//                    "Autodraw",
//                    "zxx-Zsye-x-emoji",
//                    "Emoji",
//                    "zxx-Zsym-x-shapes",
//                    "Shapes");
    @VisibleForTesting
    final StrokeManager strokeManager = new StrokeManager();
//    private ArrayAdapter<ModelLanguageContainer> languageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.for_test);

//        Spinner languageSpinner = findViewById(R.id.languages_spinner);

        DrawingView drawingView = findViewById(R.id.drawing_view);

        StatusTextView statusTextView = findViewById(R.id.status_text_view);

        drawingView.setStrokeManager(strokeManager);
        statusTextView.setStrokeManager(strokeManager);

        strokeManager.setStatusChangedListener(statusTextView);
        strokeManager.setContentChangedListener(drawingView);

        strokeManager.setActiveModel();

//        strokeManager.setDownloadedModelsChangedListener(this);

//        strokeManager.setClearCurrentInkAfterRecognition(true);
//        strokeManager.setTriggerRecognitionAfterInput(false);

//        languageAdapter = populateLanguageAdapter();
//        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//        languageSpinner.setAdapter(languageAdapter);
//        strokeManager.refreshDownloadedModelsStatus();

//        languageSpinner.setOnItemSelectedListener(
//                new OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        String languageCode =
//                                ((ModelLanguageContainer) parent.getAdapter().getItem(position)).getLanguageTag();
//                        if (languageCode == null) {
//                            return;
//                        }
//                        Log.i(TAG, "Selected language: " + languageCode);
//                        strokeManager.setActiveModel(languageCode);
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//                        Log.i(TAG, "No language selected");
//                    }
//                });

        strokeManager.reset();
    }

//    private static class ModelLanguageContainer implements Comparable<ModelLanguageContainer> {
//        private final String label;
//        @Nullable
//        private final String languageTag;
//        private boolean downloaded;
//
//        private ModelLanguageContainer(String label, @Nullable String languageTag) {
//            this.label = label;
//            this.languageTag = languageTag;
//        }
//
//        /**
//         * Populates and returns a real model identifier, with label, language tag and downloaded
//         * status.
//         */
//        public static ModelLanguageContainer createModelContainer(String label, String languageTag) {
//            // Offset the actual language labels for better readability
//            return new ModelLanguageContainer(label, languageTag);
//        }
//
//        /**
//         * Populates and returns a label only, without a language tag.
//         */
//        public static ModelLanguageContainer createLabelOnly(String label) {
//            return new ModelLanguageContainer(label, null);
//        }
//
//        @Nullable
//        public String getLanguageTag() {
//            return languageTag;
//        }
//
////        public void setDownloaded(boolean downloaded) {
////            this.downloaded = downloaded;
////        }
//
//        @NonNull
//        @Override
//        public String toString() {
//            if (languageTag == null) {
//                return label;
//            } else if (downloaded) {
//                return "   [D] " + label;
//            } else {
//                return "   " + label;
//            }
//        }
//
//        @Override
//        public int compareTo(ModelLanguageContainer o) {
//            return label.compareTo(o.label);
//        }
//    }

//    public void downloadClick(View v) {
//        strokeManager.downloadModel();
//    }

    public void recognizeClick(View v) {
        strokeManager.recognize();
    }

    public void clearClick(View v) {
        strokeManager.reset();
        DrawingView drawingView = findViewById(R.id.drawing_view);
        drawingView.clear();
    }

//    public void deleteClick(View v) {
//        strokeManager.deleteModel();
//    }

//    @Override
//    public void onDownloadedModelsChanged(Set<String> downloadedLanguageTags) {
//        for (int i = 0; i < languageAdapter.getCount(); i++) {
//            ModelLanguageContainer container = languageAdapter.getItem(i);
//
//            assert container != null;
//
//            container.setDownloaded(downloadedLanguageTags.contains(container.languageTag));
//        }
//        languageAdapter.notifyDataSetChanged();
//    }

//    private ArrayAdapter<ModelLanguageContainer> populateLanguageAdapter() {
//
//        ArrayAdapter<ModelLanguageContainer> languageAdapter =
//                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
//
//        languageAdapter.add(ModelLanguageContainer.createLabelOnly("Select language"));
////        languageAdapter.add(ModelLanguageContainer.createLabelOnly("Non-text Models"));
////
////        // Manually add non-text models first
////        for (String languageTag : NON_TEXT_MODELS.keySet()) {
////            languageAdapter.add(
////                    ModelLanguageContainer.createModelContainer(
////                            NON_TEXT_MODELS.get(languageTag), languageTag));
////        }
////
////
//        languageAdapter.add(ModelLanguageContainer.createLabelOnly("Text Models"));
//
//        ImmutableSortedSet.Builder<ModelLanguageContainer> textModels =
//                ImmutableSortedSet.naturalOrder();
//        for (DigitalInkRecognitionModelIdentifier modelIdentifier :
//                DigitalInkRecognitionModelIdentifier.allModelIdentifiers()) {
//            if (NON_TEXT_MODELS.containsKey(modelIdentifier.getLanguageTag())) {
//                continue;
//            }
//            if (modelIdentifier.getLanguageTag().endsWith(GESTURE_EXTENSION)) {
//                continue;
//            }
//
//            textModels.add(buildModelContainer(modelIdentifier, "Script"));
//        }
//        languageAdapter.addAll(textModels.build());
//
////        languageAdapter.add(ModelLanguageContainer.createLabelOnly("Gesture Models"));
////
////        ImmutableSortedSet.Builder<ModelLanguageContainer> gestureModels =
////                ImmutableSortedSet.naturalOrder();
////        for (DigitalInkRecognitionModelIdentifier modelIdentifier :
////                DigitalInkRecognitionModelIdentifier.allModelIdentifiers()) {
////            if (!modelIdentifier.getLanguageTag().endsWith(GESTURE_EXTENSION)) {
////                continue;
////            }
////
////            gestureModels.add(buildModelContainer(modelIdentifier, "Script gesture classifier"));
////        }
////
////        languageAdapter.addAll(gestureModels.build());
//
//        return languageAdapter;
//    }

//    private static ModelLanguageContainer buildModelContainer(
//            DigitalInkRecognitionModelIdentifier modelIdentifier, String labelSuffix) {
//        StringBuilder label = new StringBuilder();
//        label.append(new Locale(modelIdentifier.getLanguageSubtag()).getDisplayName());
//        if (modelIdentifier.getRegionSubtag() != null) {
//            label.append(" (").append(modelIdentifier.getRegionSubtag()).append(")");
//        }
//
//        if (modelIdentifier.getScriptSubtag() != null) {
//            label.append(", ").append(modelIdentifier.getScriptSubtag()).append(" ").append(labelSuffix);
//        }
//        return ModelLanguageContainer.createModelContainer(
//                label.toString(), modelIdentifier.getLanguageTag());
//    }

    @Override
    protected void onStart() {
        super.onStart();
        DrawingView view = findViewById(R.id.drawing_view);

        String words = """
                She walks in beauty, like the night
                Of cloudless climes and starry skies;
                And all that’s best of dark and bright
                Meet in her aspect and her eyes;
                Thus mellowed to that tender light
                Which heaven to gaudy day denies.

                One shade the more, one ray the less,
                Had half impaired the nameless grace
                Which waves in every raven tress,
                Or softly lightens o’er her face;
                Where thoughts serenely sweet express,
                How pure, how dear their dwelling-place.

                And on that cheek, and o’er that brow,
                So soft, so calm, yet eloquent,
                The smiles that win, the tints that glow,
                But tell of days in goodness spent,
                A mind at peace with all below,
                A heart whose love is innocent!
                 """;

        view.setText(words);
    }

}
