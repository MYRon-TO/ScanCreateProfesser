package com.example.scancreateprofessor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;

import model.UriStringConverters;
import model.database.DataEntityNote;
import model.preference.PreferenceManager;
import presenter.NoteManager;
import view.AddNoteDialog;
import view.folderrecycleview.FolderNoteCardAdapter;
import view.folderrecycleview.FolderNoteCardElement;

public class FolderActivity extends AppCompatActivity implements AddNoteDialog.AddNoteDialogListener {
    private final static String TAG = "FolderActivity";
    private final PreferenceManager preference = PreferenceManager.getInstance();
    private final ActivityResultLauncher<Intent> permissionActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri treeUri = result.getData() != null ? result.getData().getData() : null;
                    if (treeUri == null) {
                        Log.d("IntentError", "Tree URI is null");
                        throw new IllegalStateException("Tree URI is null");
                    }
                    getContentResolver().takePersistableUriPermission(
                            treeUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    );
                    preference.setPreferencePathToSaveNote(treeUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        MaterialToolbar appBar = findViewById(R.id.top_app_bar_activity_folder);
        appBar.setOnMenuItemClickListener(
                menuItem -> {
                    Log.d(TAG + "/appBar", String.valueOf(menuItem.getItemId()));
                    if (menuItem.getItemId() == R.id.preference_menu_app_bar_activity_folder) {
                        startActivity(new Intent(this, PreferenceActivity.class));
                    }
                    return true;
                }
        );

        try {
            if (preference.getPreferenceIsFirstTime()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Several Step to Start")
                        .setMessage("Select a folder to save your note")
                        .setPositiveButton(
                                "OK",
                                (dialog, which) -> {
                                    Log.d("MainActivity", "OK");

                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                                    permissionActivityResultLauncher.launch(intent);
                                    preference.setPreferenceIsFirstTime(false);
                                }
                        )
                        .show();
            }
        } catch (Exception e) {
            Log.d("MainActivity", "Error: " + e.getMessage());
            // panic when error
            throw e;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        RecyclerView recyclerView = findViewById(R.id.recyclerview_activity_folder);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL));

        FolderNoteCardAdapter adapter = new FolderNoteCardAdapter(
                R.layout.folder_note_card_row_item,
                setFolderNoteCardElementData(),
                this
        );

        recyclerView.setAdapter(adapter);

        ExtendedFloatingActionButton newFab = findViewById(R.id.add_extended_fab_activity_folder);
        newFab.setOnClickListener(
                v -> {
                    AddNoteDialog dialog = new AddNoteDialog();
                    dialog.show(getSupportFragmentManager(), "AddNoteDialog");
                }
        );
    }

    private ArrayList<FolderNoteCardElement> setFolderNoteCardElementData() {
        ArrayList<FolderNoteCardElement> noteArray = new ArrayList<>();

        ArrayList<DataEntityNote> dataList = NoteManager.getInstance().getAllNotes();

        for (DataEntityNote data : dataList) {
            try {
                String title = data.getTitle();
                String content = NoteManager.getInstance().previewNote(data.getNoteFile()).get();
                Uri uri = data.getNoteFile();
                noteArray.add(new FolderNoteCardElement(title, content, uri));
            } catch (Exception e) {
                Log.e(TAG, "Error: Can Not Preview Note" + e.getMessage());
            }
        }
        return noteArray;
    }

    @Override
    public void onAddNoteDialogPositiveClick(String fileTitle) {
        Log.i(TAG, "onAddNoteDialogPositiveClick! FileTitle: " + fileTitle);
        Futures.addCallback(
                NoteManager.getInstance().addNote(fileTitle),
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(Uri result) {
                        Intent intent = new Intent(FolderActivity.this, NoteActivity.class);

                        Log.d(TAG + "/intent", UriStringConverters.stringFromUri(result));
                        intent.putExtra("FileUri", UriStringConverters.stringFromUri(result));
                        intent.putExtra("Title", fileTitle);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        Log.e(TAG, "Error: Can Not Add Note" + t.getMessage());
                    }
                },
                MoreExecutors.directExecutor()
        );
    }

}
