package com.example.scancreateprofessor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.scancreateprofessor.folderrecycleview.FolderNoteCardAdapter;
import com.example.scancreateprofessor.folderrecycleview.FolderNoteCardElement;

import java.util.ArrayList;

import presenter.NoteManager;

public class FolderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
    }


    @Override
    protected void onStart() {
        super.onStart();

        RecyclerView recyclerView = findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL));

        FolderNoteCardAdapter adapter = new FolderNoteCardAdapter(
                R.layout.folder_note_card_row_item,
                setFolderNoteCardElementData()
        );

        recyclerView.setAdapter(adapter);
    }

    private ArrayList<FolderNoteCardElement> setFolderNoteCardElementData() {
        ArrayList<FolderNoteCardElement> data = new ArrayList<>();

//        NoteManager.getInstance().

        data.add(new FolderNoteCardElement("1", "Google"));
        data.add(new FolderNoteCardElement("2", "Huawei"));
        data.add(new FolderNoteCardElement("3", "Xiaomi"));

        return data;
    }

}
