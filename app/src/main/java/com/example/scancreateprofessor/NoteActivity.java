package com.example.scancreateprofessor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import model.UriStringConverters;
import presenter.NoteManager;
import presenter.digitalink.StrokeManager;
import view.DrawingView;
import view.StatusTextView;

public class NoteActivity extends AppCompatActivity {
    private static final String TAG = "NoteActivity";
    @VisibleForTesting
    final StrokeManager strokeManager = StrokeManager.getInstance();
    DrawingView drawingView;

    private Uri fileUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String intentMessage = getIntent().getStringExtra("FileUri");
        fileUri = UriStringConverters.uriFromString(intentMessage);
        String fileTitle = getIntent().getStringExtra("Title");

        Log.d(TAG + "/intent", "FileUri: " + fileUri);

//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note);

        drawingView = findViewById(R.id.drawing_view_activity_note);
        drawingView.setFileUri(fileUri);

        StatusTextView statusTextView = new StatusTextView(findViewById(R.id.coordinator_layout_activity_note));

        statusTextView.setStrokeManager(strokeManager);
        strokeManager.setStatusChangedListener(statusTextView);
        strokeManager.setContentChangedListener(drawingView);
        strokeManager.setActiveModel();
        strokeManager.reset();

        drawingView.setStrokeManager(strokeManager);

        MaterialToolbar appBar = findViewById(R.id.top_app_bar_activity_note);
        appBar.setTitle(fileTitle);
        appBar.setNavigationOnClickListener(
                v -> {
                    Intent intent = new Intent(NoteActivity.this, FolderActivity.class);
                    startActivity(intent);
                }
        );

    }


    public void recognizeClick(View v) {
        strokeManager.recognize();
        drawingView.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
        strokeManager.setStatus("Ready");

        Futures.addCallback(
                NoteManager.getInstance().readNote(fileUri),
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(String result) {
                        String content = result == null ? "" : result;
                        drawingView.setText(content);
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        Log.e(TAG, "Error: Can Not Read Note" + t.getMessage());
                    }
                },
                MoreExecutors.directExecutor()
        );
    }

}
