package com.example.scancreateprofessor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    private static final int PERMISSION_REQUEST_CODE = 8000;
    @VisibleForTesting
    final StrokeManager strokeManager = StrokeManager.getInstance();
    final TextAnalysis textAnalysis = new TextAnalysis();

    DrawingView drawingView;

    private Uri fileUri;
    private String fileTitle;
    private Uri imageUri;
    private String noteContent;
    private final ActivityResultLauncher<Intent> goAlbumResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        imageUri = result.getData().getData();
                        handleScan();
                    }
                }
            }
    );

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

        //获取资源字符串
        String popupWindowTitle=getResources().getString(R.string.scan_popup_window_title);
        String popupWindowCamera=getResources().getString(R.string.scan_popup_window_item_camera);
        String popupWindowAlbum=getResources().getString(R.string.scan_popup_window_item_Album);
        String popupWindowCancel=getResources().getString(R.string.scan_popup_window_cancel);

//        ** scan button
        ExtendedFloatingActionButton scanButton = findViewById(R.id.scan_extended_fab_activity_note);
        scanButton.setOnClickListener(
                v -> new MaterialAlertDialogBuilder(this)
                        .setTitle(popupWindowTitle)
//                            .setMessage("Choose where to scan the image from.")
                        .setItems(
                                new CharSequence[]{
                                        popupWindowCamera,
                                        popupWindowAlbum
                                },
                                (dialog, which) -> {
                                    switch (which) {
                                        case 0:
                                            goCamera();
                                            break;
                                        case 1:
                                            goAlbum();
                                            break;
                                    }
                                }
                        )
                        .setNegativeButton(
                                popupWindowCancel,
                                (dialog, which) -> dialog.dismiss()
                        )
                        .show()
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
        String status=getResources().getString(R.string.activity_note_status);
        strokeManager.setStatus(status);

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
        NoteManager.getInstance().updateNote(fileUri, noteContent);
    }

    private void goCamera(){
        checkPermission(Permission.Camera);
    }

    private void goCameraIntent() {
        Intent intent = new Intent(NoteActivity.this, CameraXActivity.class);
        // *** hang on the note activity
        intent.putExtra("FileUri", UriStringConverters.stringFromUri(fileUri));
        intent.putExtra("Title", fileTitle);
        startActivity(intent);
    }

    private void goAlbum(){
        checkPermission(Permission.ReadExternalStorage);

        Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
        intent1.setType("image/*");
        goAlbumResultLauncher.launch(intent1);
    }

    enum Permission{
        Camera,
        ReadExternalStorage
    }

    private void checkPermission(@NonNull Permission permission){
        switch (permission){
            case Camera -> {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG,"NO CAMERA PERMISSION");
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{android.Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CODE
                    );
                }else {
                    goCameraIntent();
                }
            }
            case ReadExternalStorage -> {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG,"NO ReadExternal Storage PERMISSION");
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE
                    );
                }
            }
        }
    }

}
