package presenter.digitalink;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.Ink;
import com.google.mlkit.vision.digitalink.RecognitionResult;

import java.util.concurrent.atomic.AtomicBoolean;

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

    public static class RecognizedInk {
        public final Ink ink;
        public final String text;

        RecognizedInk(Ink ink, String text) {
            this.ink = ink;
            this.text = text;
        }
    }

    public static class TaskToFuture {
        public static <T> ListenableFuture<T> toListenableFuture(Task<T> task) {
            SettableFuture<T> future = SettableFuture.create();
            task.addOnSuccessListener(new OnSuccessListener<T>() {
                @Override
                public void onSuccess(T result) {
                    future.set(result);
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    future.setException(e);
                }
            });
            return future;
        }
    }

    public ListenableFuture<ListenableFuture<String>> run() {
        Log.i(TAG, "RecoTask.run");

        ListenableFuture<RecognitionResult> future = TaskToFuture.toListenableFuture(recognizer.recognize(this.ink));

        return Futures.transform(
                future,
                result -> {
                    if (cancelled.get() || result.getCandidates().isEmpty()) {
                        return Futures.immediateFuture(null);
                    }
                    currentResult = new RecognizedInk(ink, result.getCandidates().get(0).getText());
                    Log.i(TAG, "result: " + currentResult.text);
                    done.set(true);
                    return Futures.immediateFuture(currentResult.text);
                },
                MoreExecutors.directExecutor()
        );
    }

}
