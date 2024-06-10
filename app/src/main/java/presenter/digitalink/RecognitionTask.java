package presenter.digitalink;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.Ink;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Task to run asynchronously to obtain recognition results.
 */
public class RecognitionTask {


    private static final String TAG = "RecognitionTask";
    private final DigitalInkRecognizer recognizer;
    private final Ink ink;
    @Nullable
    private RecognizedInk currentResult;
    private final AtomicBoolean cancelled;
    private final AtomicBoolean done;

    public RecognitionTask(DigitalInkRecognizer recognizer, Ink ink) {
        this.recognizer = recognizer;
        this.ink = ink;
        this.currentResult = null;
        cancelled = new AtomicBoolean(false);
        done = new AtomicBoolean(false);
    }

    public void cancel() {
        cancelled.set(true);
    }

    public boolean done() {
        return done.get();
    }

    @Nullable
    public RecognizedInk result() {
        return this.currentResult;
    }

    /**
     * Helper class that stores an ink along with the corresponding recognized text.
     */
    public static class RecognizedInk {

        public enum InkType {
            TEXT,
            Gesture,
            TEXT_BUT_GESTURE
        }

        public final Ink ink;
        public final String text;
        public final InkType type;

        RecognizedInk(Ink ink, String text, InkType type) {
            this.ink = ink;
            this.text = text;
            this.type = type;
        }
    }

    public Task<RecognizedInk> run(Boolean isGesture) {
        Log.i(TAG, "RecoTask.run");
        return recognizer
                .recognize(this.ink)
                .onSuccessTask(
                        result -> {
                            if (cancelled.get() || result.getCandidates().isEmpty()) {
                                return Tasks.forResult(null);
                            }
                            if (isGesture) {
                                String resultText = result.getCandidates().get(0).getText();

                                RecognizedInk.InkType type = resultText.equals("writing") ? RecognizedInk.InkType.TEXT_BUT_GESTURE : RecognizedInk.InkType.Gesture;

                                done.set(type != RecognizedInk.InkType.TEXT_BUT_GESTURE);
                                currentResult = new RecognizedInk(
                                        ink,
                                        resultText,
                                        type
                                );
                            } else {
                                currentResult = new RecognizedInk(
                                        ink,
                                        result.getCandidates().get(0).getText(),
                                        RecognizedInk.InkType.TEXT

                                );
                                done.set(true);
                            }
                            Log.i(TAG,"type" + currentResult.type.toString() + "\t" + "result: " + currentResult.text);
                            return Tasks.forResult(currentResult);
                        });
    }
}