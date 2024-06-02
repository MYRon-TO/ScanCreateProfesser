package com.example.scancreateprofessor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

public class Folder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder);
    }


    @Override
    protected void onStart() {
        super.onStart();

        RecyclerView recyclerView = findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL));

        CustomAdapter adapter = new CustomAdapter(
                R.layout.note_row_item,
                getComplexData()
        );

        recyclerView.setAdapter(adapter);
    }

    private ArrayList<DataElement> getComplexData() {
        ArrayList<DataElement> data =
                new ArrayList<>();
        data.add(new DataElement("1", "Google"));
        data.add(new DataElement("2", "Huawei"));
        data.add(new DataElement("3", "Xiaomi"));

        return data;
    }


    private ArrayList<CharSequence> getData() {
        ArrayList<CharSequence> data =
                new ArrayList<>();
        data.add("Google");
        data.add("Huawei");
        data.add("Xiaomi");

        return data;
    }


}
