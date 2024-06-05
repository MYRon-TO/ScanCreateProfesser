package com.example.scancreateprofessor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import view.PreferenceFragment;

public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_preference);

        MaterialToolbar appBar = findViewById(R.id.top_app_bar_activity_preference);
        appBar.setNavigationOnClickListener(
                v -> {
                    Intent intent = new Intent(PreferenceActivity.this, FolderActivity.class);
                    startActivity(intent);

                }
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.preference_container, new PreferenceFragment())
                    .commit();
        }
    }
}