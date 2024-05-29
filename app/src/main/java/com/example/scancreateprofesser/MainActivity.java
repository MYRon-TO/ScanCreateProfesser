package com.example.scancreateprofesser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutionException;

import model.preference.PreferenceManager;
import presenter.NoteManager;

public class MainActivity extends AppCompatActivity {
    PreferenceManager preference;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
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

        preference = PreferenceManager.getInstance(this);
//        PreferenceManager.getInstance(this).setPreferenceIsFirstTime(true);

        try {
            if (preference.getPreferenceIsFirstTime()) {
//            if (true) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                activityResultLauncher.launch(intent);
                preference.setPreferenceIsFirstTime(false);
            }
        } catch (Exception e) {
            Log.d("MainActivity", "Error: " + e.getMessage());
            // panic when error
            throw e;
        }

        setContentView(R.layout.for_test);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Futures.addCallback(
                NoteManager.getInstance().addNote("TestFile123"),
                new FutureCallback<Uri> () {
                    @Override
                    public void onSuccess(Uri result) {

                        Log.d("MainActivity/WriteNote/UriResult", "Note added: " + result.toString());

                        try {
                            NoteManager.getInstance().updateNote( result, "TestFile123" ).get();
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        Log.d("MainActivity/ReadNote/UriResult", "Note added: " + result);

                        try {
                            String Note = NoteManager.getInstance().readNote( result ).get();

                            TextView tv = findViewById(R.id.forTestShowUri);
                            tv.setText(Note);

                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }
                    @Override
                    public void onFailure(Throwable t) {
                        Log.d("MainActivity/AddedNote", "Error: " + t.getMessage());
                    }
                },
                MoreExecutors.directExecutor()
        );

    }
}