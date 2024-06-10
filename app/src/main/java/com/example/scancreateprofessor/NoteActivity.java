package com.example.scancreateprofessor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import model.UriStringConverters;
import presenter.NoteManager;
import presenter.TextAnalysis;
import presenter.digitalink.StrokeManager;
import view.DrawingView;
import view.StatusTextView;

public class NoteActivity extends AppCompatActivity {
    private static final String TAG = "NoteActivity";
    @VisibleForTesting
    final StrokeManager strokeManager = StrokeManager.getInstance();
    final TextAnalysis textAnalysis = new TextAnalysis();

    DrawingView drawingView;

    private Uri fileUri;
    private String fileTitle;
    private Uri imageUri;
    private String noteContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

//        ** get intent
        fileUri = UriStringConverters.uriFromString(getIntent().getStringExtra("FileUri"));
        fileTitle = getIntent().getStringExtra("Title");
        imageUri = UriStringConverters.uriFromString(getIntent().getStringExtra("ScanResult"));

        Log.d(TAG + "/intent", "FileUri: " + fileUri);

//        ** set content view
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

//        ** app bar
        MaterialToolbar appBar = findViewById(R.id.top_app_bar_activity_note);
        appBar.setTitle(fileTitle);
        appBar.setNavigationOnClickListener(
                v -> {
                    Intent intent = new Intent(NoteActivity.this, FolderActivity.class);
                    startActivity(intent);
                }
        );

//        ** scan button
        ExtendedFloatingActionButton scanButton = findViewById(R.id.scan_extended_fab_activity_note);
        scanButton.setOnClickListener(
                v -> {
                    // *** hang on the note activity
                    Intent intent = new Intent(NoteActivity.this, CameraXActivity.class);
                    intent.putExtra("FileUri", UriStringConverters.stringFromUri(fileUri));
                    intent.putExtra("Title", fileTitle);
                    startActivity(intent);
                }
        );

//        ** recognize button
        FloatingActionButton recognizeButton = findViewById(R.id.recognize_floating_action_button_activity_note);
        recognizeButton.setOnClickListener(
                v -> {
                    strokeManager.recognize();
                    drawingView.clear();
                }
        );

    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        strokeManager.setStatus("Ready");

        Futures.addCallback(
                NoteManager.getInstance().readNote(fileUri),
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(String result) {
                        noteContent = result == null ? "" : result;
                        handleScan();
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        Log.e(TAG, "Error: Can Not Read Note" + t.getMessage());
                    }
                },
                MoreExecutors.directExecutor()
        );
    }

    private void handleScan() {
        if (imageUri != null) {
            try {
                textAnalysis
                        .imageUriTextAnalyzer(imageUri, this)
                        .addOnSuccessListener(
                                result -> {
                                    Log.d(TAG + "handleScan/TextAnalysis", result.getText());
                                    noteContent = noteContent.concat(result.getText());
                                    refreshContent();
                                }
                        );
            } catch (Exception e) {
                Log.e(TAG + "/handleScan/TextAnalysis", "Error: Can Not Handle Scan " + e.getMessage());
//                return null;
            } finally {
                imageUri = null;
            }
        } else {
            refreshContent();
//            return null;
        }
    }

    private void refreshContent() {
        Log.d(TAG + "refreshContent", noteContent);
        drawingView.setText(noteContent);
    }

}
