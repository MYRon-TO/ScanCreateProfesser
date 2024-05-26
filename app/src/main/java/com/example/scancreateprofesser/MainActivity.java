package com.example.scancreateprofesser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

        try {
            if (preference.getPreferenceIsFirstTime()) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
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

        Button btAdd = findViewById(R.id.forTestButtonAdd);
        EditText etTitle = findViewById(R.id.forTestTitle);
        btAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            if (title.isEmpty()) {
                return;
            }
            NoteManager.getInstance().addNote(title);
        });

        Button btWrite = findViewById(R.id.forTestButtonWrite);
        EditText etWrite = findViewById(R.id.forTestContent);

        try {
            Uri aUri = NoteManager.getInstance().giveMeAnUri();

            TextView tv = findViewById(R.id.forTestShowUri);
            tv.setText(aUri.toString());

            btWrite.setOnClickListener(v -> {
                String content = etWrite.getText().toString();
                if (content.isEmpty()) {
                    Log.e("MainActivity", "Content is empty");
                    return;
                }

                NoteManager.getInstance().updateNote(
                        aUri,
                        content
                );
            });

            Button btRead = findViewById(R.id.forTestButtonLoad);
            btRead.setOnClickListener(v -> {
                String content = null;
                try {
                    content = NoteManager.getInstance().readNote(
                            aUri
                    ).get();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                etWrite.setText(content);
            });

        }catch (Exception e){
            Log.e("MainActivity:GiveMeAUri", "Error: " + e.getMessage());
        }

    }
}