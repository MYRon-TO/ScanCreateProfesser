package presenter.digitalink;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.mlkit.vision.digitalink.Ink;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import model.digitalinkmanager.DigitalInkModelManager;
import model.digitalinkmanager.ModelManager;

public class StrokeManager {

    public interface ContentChangedListener {
        void onContentChanged();
    }

    public interface StatusChangedListener {
        void onStatusChanged();
    }

    public interface DownloadedModelsChangedListener {
        void onDownloadedModelsChanged(Set<String> downloadedLanguageTags);
    }

    @VisibleForTesting
    static final long CONVERSION_TIMEOUT_MS = 1000;
    private static final String TAG = "StrokeManager";
    private static final int TIMEOUT_TRIGGER = 1;
    private RecognitionTask recognitionTask = null;
    @VisibleForTesting
    DigitalInkModelManager modelManager = new DigitalInkModelManager();
    private final List<RecognitionTask.RecognizedInk> content = new ArrayList<>();
    private Ink.Stroke.Builder strokeBuilder = Ink.Stroke.builder();
    private Ink.Builder inkBuilder = Ink.builder();
    private boolean stateChangedSinceLastRequest = false;
    @Nullable private ContentChangedListener contentChangedListener = null;
    @Nullable private StatusChangedListener statusChangedListener = null;
    @Nullable
    private DownloadedModelsChangedListener downloadedModelsChangedListener = null;

    private boolean triggerRecognitionAfterInput = true;
    private boolean clearCurrentInkAfterRecognition = true;
    private String status = "";

    public void setTriggerRecognitionAfterInput(boolean shouldTrigger) {
        triggerRecognitionAfterInput = shouldTrigger;
    }

    public void setClearCurrentInkAfterRecognition(boolean shouldClear) {
        clearCurrentInkAfterRecognition = shouldClear;
    }

    private final Handler uiHandler =
            new Handler(
                    msg -> {
                        if (msg.what == TIMEOUT_TRIGGER) {
                            Log.i(TAG, "Handling timeout trigger.");
                            commitResult();
                            return true;
                        }
                        return false;
                    });


    private void setStatus(String newStatus) {
        status = newStatus;
        if (statusChangedListener != null) {
            statusChangedListener.onStatusChanged();
        }
    }

    private void commitResult() {
        if (recognitionTask.done() && recognitionTask.result() != null) {
            content.add(recognitionTask.result());
            setStatus("Successful recognition: " + recognitionTask.result().text);
            if (clearCurrentInkAfterRecognition) {
                resetCurrentInk();
            }
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
        setStatus("");
    }

    private void resetCurrentInk() {
        inkBuilder = Ink.builder();
        strokeBuilder = Ink.Stroke.builder();
        stateChangedSinceLastRequest = false;
    }

    public Ink getCurrentInk() {
        return inkBuilder.build();
    }


    public boolean addNewTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        long t = System.currentTimeMillis();

        uiHandler.removeMessages(TIMEOUT_TRIGGER);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                break;
            case MotionEvent.ACTION_UP:
                strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                inkBuilder.addStroke(strokeBuilder.build());
                strokeBuilder = Ink.Stroke.builder();
                stateChangedSinceLastRequest = true;
                if (triggerRecognitionAfterInput) {
                    recognize();
                }
                break;
            default:
                return false;
        }

        return true;
    }

    public void setContentChangedListener(ContentChangedListener contentChangedListener) {
        this.contentChangedListener = contentChangedListener;
    }

    public void setStatusChangedListener(StatusChangedListener statusChangedListener) {
        this.statusChangedListener = statusChangedListener;
    }

    public void setDownloadedModelsChangedListener(
            DownloadedModelsChangedListener downloadedModelsChangedListener) {
        this.downloadedModelsChangedListener = downloadedModelsChangedListener;
    }

    public List<RecognitionTask.RecognizedInk> getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }


}
