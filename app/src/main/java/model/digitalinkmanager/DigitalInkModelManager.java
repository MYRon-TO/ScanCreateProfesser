package model.digitalinkmanager;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to manage model downloading, deletion, and selection.
 */
public class DigitalInkModelManager {

    private static final String TAG = "DigitalInkModelManager";
    private DigitalInkRecognitionModel model;
    private DigitalInkRecognitionModel gestureModel;
    private DigitalInkRecognizer recognizer;
    private DigitalInkRecognizer gestureRecognizer;
    final RemoteModelManager remoteModelManager = RemoteModelManager.getInstance();

    public String setModel(String languageTag) {
        // Clear the old model and recognizer.
        model = null;
        if (recognizer != null) {
            recognizer.close();
        }

        if (gestureRecognizer != null) {
            gestureRecognizer.close();
        }

        recognizer = null;
        gestureRecognizer = null;

        String gestureLanguageTag = languageTag + "-x-gesture";

        try {
            setSingleModel(languageTag);
            setSingleModel(gestureLanguageTag);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to set model for language '" + languageTag + "'");

            return "Failed to set model for language '" + languageTag + "'";

            // TODO: 6/1/24 throw an exception here

        }

        return "Model set for language: " + languageTag;

    }

    public void setSingleModel(String languageTag) {
        // Try to parse the languageTag and get a model from it.
        DigitalInkRecognitionModelIdentifier modelIdentifier;
        try {
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag);
        } catch (MlKitException e) {
            Log.e(TAG, "Failed to parse language '" + languageTag + "'");
            throw new RuntimeException("Failed to parse language '" + languageTag + "'");
        }
        if (modelIdentifier == null) {
            throw new RuntimeException("Model not found for language '" + languageTag + "'");
        }

        // Initialize the model and recognizer.
        model = DigitalInkRecognitionModel.builder(modelIdentifier).build();
        recognizer =
                DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(model).build());
        Log.i(
                TAG,
                "Model set for language '"
                        + languageTag
                        + "' ('"
                        + modelIdentifier.getLanguageTag()
                        + "').");
    }

    public DigitalInkRecognizer getRecognizer() {
        return recognizer;
    }

    public Task<Boolean> checkIsModelDownloaded() {
        return remoteModelManager.isModelDownloaded(model);
    }

    public Task<String> deleteActiveModel() {
        if (model == null) {
            Log.i(TAG, "Model not set");
            return Tasks.forResult("Model not set");
        }
        return checkIsModelDownloaded()
                .onSuccessTask(
                        result -> {
                            if (!result) {
                                return Tasks.forResult("Model not downloaded yet");
                            }
                            return remoteModelManager
                                    .deleteDownloadedModel(model)
                                    .onSuccessTask(
                                            aVoid -> {
                                                Log.i(TAG, "Model successfully deleted");
                                                return Tasks.forResult("Model successfully deleted");
                                            });
                        })
                .addOnFailureListener(e -> Log.e(TAG, "Error while model deletion: " + e));
    }

    public Task<Set<String>> getDownloadedModelLanguages() {
        return remoteModelManager
                .getDownloadedModels(DigitalInkRecognitionModel.class)
                .onSuccessTask(
                        (remoteModels) -> {
                            Set<String> result = new HashSet<>();
                            for (DigitalInkRecognitionModel model : remoteModels) {
                                result.add(model.getModelIdentifier().getLanguageTag());
                            }
                            Log.i(TAG, "Downloaded models for languages:" + result);
                            return Tasks.forResult(result);
                        });
    }

    public Task<String> download() {
        if (model == null) {
            return Tasks.forResult("Model not selected.");
        }
        return remoteModelManager
                .download(model, new DownloadConditions.Builder().build())
                .onSuccessTask(
                        aVoid -> {
                            Log.i(TAG, "Model download succeeded.");
                            return Tasks.forResult("Downloaded model successfully");
                        })
                .addOnFailureListener(e -> Log.e(TAG, "Error while downloading the model: " + e));
    }
}