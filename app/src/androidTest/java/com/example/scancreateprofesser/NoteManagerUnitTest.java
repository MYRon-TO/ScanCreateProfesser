package com.example.scancreateprofesser;

import android.app.Activity;
import android.content.Intent;

import androidx.core.app.ActivityCompat;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import presenter.NoteManager;

public class NoteManagerUnitTest {

    private final NoteManager noteManager = NoteManager.getInstance();

    @Before
    public void setUp() {
    }

    @Test
    public void createFile() throws ExecutionException, InterruptedException {
        boolean result = noteManager.addNote("testNote.txt").get();
        assert(result);
    }
}
