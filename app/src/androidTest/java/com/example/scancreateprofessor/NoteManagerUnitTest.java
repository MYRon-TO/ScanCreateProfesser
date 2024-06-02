package com.example.scancreateprofessor;

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
        noteManager.addNote("testNote.txt").get();
//        assert(result);
    }
}
