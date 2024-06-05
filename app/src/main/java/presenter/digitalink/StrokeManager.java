package presenter.digitalink;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.digitalink.Ink;
import com.google.mlkit.vision.digitalink.Ink.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import model.digitalinkmanager.DigitalInkModelManager;
import model.preference.PreferenceManager;
import view.DrawingView;

/**
 * Manages the recognition logic and the content that has been added to the current page.
 */
public class StrokeManager {

//    private StrokeManager strokeManager;
    private static volatile StrokeManager instance;

    public static StrokeManager getInstance(){
        if (instance != null) {
            return instance;
        }else{
            synchronized (StrokeManager.class) {
                instance = new StrokeManager();
            }
        }
        return instance;
    }

    public StrokeManager(){
    }


    /**
     * Interface to register to be notified of changes in the recognized content.
     */
    public interface ContentChangedListener {

        /**
         * This method is called when the recognized content changes.
         */
        void onContentChanged();
    }

    /**
     * Interface to register to be notified of changes in the status.
     */
    public interface StatusChangedListener {

        /**
         * This method is called when the recognized content changes.
         */
        void onStatusChanged();
    }

    /**
     * Interface to register to be notified of changes in the downloaded model state.
     */
    public interface DownloadedModelsChangedListener {

        /**
         * This method is called when the downloaded models changes.
         */
        void onDownloadedModelsChanged(Set<String> downloadedLanguageTags);
    }

    @VisibleForTesting
    static final long CONVERSION_TIMEOUT_MS = 1000;
    private static final String TAG = "StrokeManager";
    // This is a constant that is used as a message identifier to trigger the timeout.
    private static final int TIMEOUT_TRIGGER = 1;
    // For handling recognition and model downloading.
    private RecognitionTask recognitionTask = null;
    @VisibleForTesting
    DigitalInkModelManager gestureModelManager = new DigitalInkModelManager();
    @VisibleForTesting
    DigitalInkModelManager writingModelManager = new DigitalInkModelManager();

    // Managing the recognition queue.
    private final List<RecognitionTask.RecognizedInk> content = new ArrayList<>();
    // Managing ink currently drawn.
    private Ink.Stroke.Builder strokeBuilder = Ink.Stroke.builder();
    private Ink.Builder inkBuilder = Ink.builder();
    private boolean stateChangedSinceLastRequest = false;
    @Nullable
    private ContentChangedListener contentChangedListener = null;
    @Nullable
    private StatusChangedListener statusChangedListener = null;
    @Nullable
    private DownloadedModelsChangedListener downloadedModelsChangedListener = null;

    private GestureHandler gestureHandler;

    private String status = "";

    // Handler to handle the UI Timeout.
    // This handler is only used to trigger the UI timeout. Each time a UI interaction happens,
    // the timer is reset by clearing the queue on this handler and sending a new delayed message (in
    // addNewTouchEvent).
    private final Handler uiHandler =
            new Handler(
                    msg -> {
                        if (msg.what == TIMEOUT_TRIGGER) {
                            Log.i(TAG, "Handling timeout trigger.");
                            commitResult();
                            return true;
                        }
                        // In the current use this statement is never reached because we only ever send
                        // TIMEOUT_TRIGGER messages to this handler.
                        // This line is necessary because otherwise Java's static analysis doesn't allow for
                        // compiling. Returning false indicates that a message wasn't handled.
                        return false;
                    });

    public void setStatus(String newStatus) {
        status = newStatus;
        if (statusChangedListener != null) {
            statusChangedListener.onStatusChanged();
        }
    }

    private void commitResult() {
        if (recognitionTask.done() && recognitionTask.result() != null) {
            content.add(recognitionTask.result());
            setStatus("Successful recognition: " + Objects.requireNonNull(recognitionTask.result()).text);

            resetCurrentInk();

            if (contentChangedListener != null) {
                contentChangedListener.onContentChanged();
            }

        }
    }

    public void reset() {
        Log.i(TAG, "reset");
        resetCurrentInk();
        content.clear();
        if (recognitionTask != null && !recognitionTask.done()) {
            recognitionTask.cancel();
        }
//        setStatus("");
    }

    private void resetCurrentInk() {
        inkBuilder = Ink.builder();
        strokeBuilder = Ink.Stroke.builder();
        stateChangedSinceLastRequest = false;
    }

    public Ink getCurrentInk() {
        return inkBuilder.build();
    }

    /**
     * This method is called when a new touch event happens on the drawing client and notifies the
     * StrokeManager of new content being added.
     *
     * <p>This method takes care of triggering the UI timeout and scheduling recognitions on the
     * background thread.
     *
     * @return whether the touch event was handled.
     */
    public boolean addNewTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        long t = System.currentTimeMillis();

        // A new event happened -> clear all pending timeout messages.
        uiHandler.removeMessages(TIMEOUT_TRIGGER);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                strokeBuilder.addPoint(Point.create(x, y, t));
                break;
            case MotionEvent.ACTION_UP:
                strokeBuilder.addPoint(Point.create(x, y, t));
                inkBuilder.addStroke(strokeBuilder.build());
                strokeBuilder = Ink.Stroke.builder();
                stateChangedSinceLastRequest = true;
                break;
            default:
                // Indicate touch event wasn't handled.
                return false;
        }

        return true;
    }

    // Listeners to update the drawing and status.
    public void setContentChangedListener(@Nullable ContentChangedListener contentChangedListener) {
        this.contentChangedListener = contentChangedListener;
    }

    public void setStatusChangedListener(@Nullable StatusChangedListener statusChangedListener) {
        this.statusChangedListener = statusChangedListener;
    }

    public void setDownloadedModelsChangedListener(
            @Nullable DownloadedModelsChangedListener downloadedModelsChangedListener) {
        this.downloadedModelsChangedListener = downloadedModelsChangedListener;
    }

    public List<RecognitionTask.RecognizedInk> getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public void setActiveModel() {
        setActiveModel(PreferenceManager.getInstance().getPreferenceDigitalInkRecognitionModel());
    }

    public void setActiveModel(String languageTag) {
        setStatus(writingModelManager.setModel(languageTag));

        String gestureModel = languageTag + "-x-gesture";
        setStatus(gestureModelManager.setModel(gestureModel));

        writingModelManager.checkIsModelDownloaded().addOnSuccessListener(
                result -> {
                    if (!result) {
                        setStatus("Writing Model not downloaded yet");
                        downloadSingleModel(writingModelManager);
                    }
                }
        );

        gestureModelManager.checkIsModelDownloaded().addOnSuccessListener(
                result -> {
                    if (!result) {
                        setStatus("Gesture Model not downloaded yet");
                        downloadSingleModel(gestureModelManager);
                    }
                }
        );

    }


    public void deleteModel() {
        this.deleteSingleModel(gestureModelManager);
        this.deleteSingleModel(writingModelManager);
    }

    private void deleteSingleModel(DigitalInkModelManager modelManager) {
        modelManager
                .deleteActiveModel()
                .addOnSuccessListener(unused -> refreshDownloadedModelsStatus())
                .onSuccessTask(
                        status -> {
                            setStatus(status);
                            return Tasks.forResult(null);
                        });
    }

    public void downloadModel() {
        this.downloadSingleModel(gestureModelManager);
        this.downloadSingleModel(writingModelManager);
    }

    private void downloadSingleModel(DigitalInkModelManager modelManager) {
        setStatus("Download started.");
        modelManager
                .download()
                .addOnSuccessListener(unused -> refreshDownloadedModelsStatus())
                .onSuccessTask(
                        status -> {
                            setStatus(status);
                            return Tasks.forResult(null);
                        });
    }

    // Recognition-related.

    private boolean isSetRecognizer() {
        return gestureModelManager.getRecognizer() != null || writingModelManager.getRecognizer() != null;
    }

    /** recognize the ink drawn on the screen. *
     */
    public void recognize(){

        try {
            recognize(true)
                    .addOnCompleteListener(
                            task -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    switch (task.getResult().type) {
                                        case Gesture:
                                        case TEXT:
                                            stateChangedSinceLastRequest = false;
                                            break;
                                        case TEXT_BUT_GESTURE:
                                            try {
                                                recognize(false);
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                            break;
                                    }
                                    uiHandler.sendMessageDelayed(
                                            uiHandler.obtainMessage(TIMEOUT_TRIGGER), CONVERSION_TIMEOUT_MS);
                                }

                            }
                    )
                    .addOnFailureListener(
                            e -> {
                                setStatus("Recognition failed: " + e.getMessage());
                                reset();
                            }
                    );
        }catch (Exception e){
            Log.e(TAG, "recognize: " + e.getMessage());
        }
    }

    public Task<RecognitionTask.RecognizedInk> recognize(boolean isGesture) throws Exception {

        if (!stateChangedSinceLastRequest || inkBuilder.isEmpty()) {
            setStatus("No recognition, ink unchanged or empty");
            throw new Exception("No recognition, ink unchanged or empty");
        }
        if (!isSetRecognizer()) {
            setStatus("Recognizer not set");
            throw new Exception("Recognizer not set");
        }

        DigitalInkModelManager modelManager = isGesture ? gestureModelManager : writingModelManager;

        return modelManager
                .checkIsModelDownloaded()
                .onSuccessTask(
                        result -> {
                            if (!result) {
                                setStatus("Model not downloaded yet");
                                throw new Exception("Model not downloaded yet");
                            }

                            recognitionTask =
                                    new RecognitionTask(modelManager.getRecognizer(), inkBuilder.build());
                            return recognitionTask.run(isGesture);
                        });
    }

    public void refreshDownloadedModelsStatus() {
        gestureModelManager
                .getDownloadedModelLanguages()
                .addOnSuccessListener(
                        downloadedLanguageTags -> {
                            if (downloadedModelsChangedListener != null) {
                                downloadedModelsChangedListener.onDownloadedModelsChanged(downloadedLanguageTags);
                            }
                        }
                );

        writingModelManager
                .getDownloadedModelLanguages()
                .addOnSuccessListener(
                        downloadedLanguageTags -> {
                            if (downloadedModelsChangedListener != null) {
                                downloadedModelsChangedListener.onDownloadedModelsChanged(downloadedLanguageTags);
                            }
                        }
                );
    }

    public void setGestureHandler(DrawingView v) {
        this.gestureHandler = new GestureHandler(v);
    }

    public void handleGesture(RecognitionTask.RecognizedInk ri) {
        switch (ri.type) {
            case Gesture:
                switch (ri.text) {
                    case "strike":
                    case "scribble":
                        Log.d(TAG, "delete");
                        gestureHandler.deleteWord(ri);
                        break;
                    case "writing":
                        Log.e(TAG, "Error Writing");
                        break;
                }
                break;
            case TEXT:
                gestureHandler.writeWord(ri);
                break;
            case TEXT_BUT_GESTURE:
                Log.e(TAG + "/GestureHandler", "Error TEXT_BUT_GESTURE");
                break;
        }
        reset();
    }
}