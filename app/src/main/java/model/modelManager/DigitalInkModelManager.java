package model.modelManager;

import android.util.Log;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;

public class DigitalInkModelManager {
    private final String LOGTAG = "DigitalInkModelManager";

    private DigitalInkModelManager() {
    }

    private static volatile DigitalInkModelManager instance = null;

    public static DigitalInkModelManager getInstance() {
        if (instance == null) {
            synchronized (DigitalInkModelManager.class) {
                if (instance == null) {
                    instance = new DigitalInkModelManager();
                }
            }
        }
        return instance;
    }

    private final RemoteModelManager remoteModelManager = RemoteModelManager.getInstance();


    /**
     * Download the model from the server
     * @param model The model to download
     */
    public void downloadModel(DigitalInkRecognitionModel model) {
        remoteModelManager.download(model, new DownloadConditions.Builder().build())
                .addOnSuccessListener(v -> {
                    // Model downloaded successfully. Okay to start inference.
                    Log.i(LOGTAG, "Model downloaded successfully. Okay to start inference.");
                })
                .addOnFailureListener(e -> {
                    // Error.
                    Log.e(LOGTAG, "Error downloading model: " + e.getMessage());
                    throw new RuntimeException(e);
                });
    }

    /**
     * Check if the model is downloaded, if not download it
     * @param model The model to check
     * @return True if the model is downloaded, false otherwise
     */
    public Boolean isModelDownloaded(DigitalInkRecognitionModel model) {
        if (remoteModelManager.isModelDownloaded(model).getResult()) {
            Log.i(LOGTAG, "Model is downloaded.");
            return true;
        } else {
            Log.i(LOGTAG, "Model is not downloaded.");
            Log.i(LOGTAG, "Downloading Model.");
            try {
                downloadModel(model);
                return true;
            } catch (Exception e) {
                Log.e(LOGTAG, "Error downloading model: " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Delete the model from the device
     * @param model The model to delete
     */
    public void deleteModel(DigitalInkRecognitionModel model) {
        remoteModelManager.deleteDownloadedModel(model)
                .addOnSuccessListener(v -> {
                    // Model deleted successfully.
                    Log.i(LOGTAG, "Model deleted successfully.");
                })
                .addOnFailureListener(e -> {
                    // Error.
                    Log.e(LOGTAG, "Error deleting model: " + e.getMessage());
                    throw new RuntimeException(e);
                });
    }


}
