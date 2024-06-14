package presenter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.room.Room;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import model.UriStringConverters;
import model.database.DaoNote;
import model.database.DataBaseMain;
import model.database.DataEntityNote;
import model.filemanager.FileManager;
import model.preference.PreferenceManager;


public class NoteManager {
    private static volatile NoteManager instance = null;

    private final Context context;

    /**
     * <h2>getInstance</h2>
     * Get the instance of the NoteManager.
     * @throws IllegalStateException If the NoteManager has not been initialized.
     * @see #init(Context)
     */
    public static NoteManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("NoteManager is not initialized");
        }
        return instance;
    }

    public static void init(Context context) {
        if (instance == null) {
            synchronized (NoteManager.class) {
                instance = new NoteManager(context);
            }
        }
    }

    // Database
    private final DataBaseMain db;
    private final DaoNote daoNote;

    // Shared Preference
    private final PreferenceManager preference;

    private NoteManager(@NonNull Context context) {
        this.context = context;
        this.db = Room.databaseBuilder(context, DataBaseMain.class, "database").build();
        daoNote= db.daoNote();
        preference = PreferenceManager.getInstance();
    }

    /**
     * <h2>addNote</h2>
     * Add a note to the database and the file system.
     *
     * @param fileName The name of the note.
     * @return A future that will return true if the note was added successfully, false otherwise.
     */
    public ListenableFuture<Uri> addNote(String fileName) {

        Callable<Uri> task = () -> {

            Uri dirToAddNote = preference.getPreferencePathToSaveNote();
            DocumentFile dir = DocumentFile.fromTreeUri(context, dirToAddNote);

            try {
                assert dir != null;
                DocumentFile file = dir.createFile("text/plain", fileName);

                assert file != null;
                Uri fileUri = file.getUri();

                Log.d("NoteManager/FileUri", "File URI: " + fileUri.toString());

                // Add note to database
                daoNote.insertNotes(new DataEntityNote(fileUri, fileName));

                return fileUri;
            } catch (AssertionError | Exception e) {
                Log.e("NoteManager", "Error adding note: " + e.getMessage());
                throw e;
            }
        };

        return Futures.submit(task, MoreExecutors.directExecutor());
    }

    /**
     * <h2>readNote</h2>
     * Read a note from the file system.
     * @param fileUri The URI of the note to read.
     * @return A future that will return the content of the note.
     */
    public ListenableFuture<String> readNote(Uri fileUri) {
        Callable<String> task = () -> {

//            DocumentFile file = DocumentFile.fromTreeUri(context, fileUri);
            DocumentFile file = DocumentFile.fromSingleUri(context, fileUri);

            try {
                assert file != null;
                ListenableFuture<String> result = FileManager.read(file.getUri(), context);
                return result.get();
            } catch (AssertionError | Exception e) {
                Log.e("NoteManager", "Error reading note: " + e.getMessage());
                return "";
            }
        };
        return Futures.submit(task, MoreExecutors.directExecutor());
    }

    /**
     * <h2>previewNote</h2>
     * Preview a note from the file system.
     * @param fileUri The URI of the note to read.
     * @see #previewNote(Uri, int)
     */
    public ListenableFuture<String> previewNote(Uri fileUri) {
        return previewNote(fileUri,5);
    }

    /**
     * <h2>previewNote</h2>
     * Preview a note from the file system.
     * @param fileUri The URI of the note to read.
     * @param numToPreview The number of lines to preview.
     */
    public ListenableFuture<String> previewNote(Uri fileUri, int numToPreview){
        Callable<String> task = () -> {

//            DocumentFile file = DocumentFile.fromTreeUri(context, fileUri);
            DocumentFile file = DocumentFile.fromSingleUri(context, fileUri);

            try {
                assert file != null;
                ListenableFuture<String> result = FileManager.preview(file.getUri(), context, numToPreview);
                return result.get();
            } catch (AssertionError | Exception e) {
                Log.e("NoteManager", "Error reading note: " + e.getMessage());
                return "";
            }
        };
        return Futures.submit(task, MoreExecutors.directExecutor());
    }

    /**
     * <h2>deleteNote</h2>
     * Delete a note from the database and the file system.
     *
     * @param fileUri The URI of the note to delete.
     * @return A future that will return true if the note was deleted successfully, false otherwise.
     */
    public ListenableFuture<Boolean> deleteNote(Uri fileUri) {
        Callable<Boolean> task = () -> {
            DocumentFile file = DocumentFile.fromTreeUri(context, fileUri);
            try {
                assert file != null;
                file.delete();

                // Add note to database
                DataEntityNote toDelete = daoNote.getNoteByUri(UriStringConverters.stringFromUri(fileUri)).get();
                daoNote.deleteNotes(toDelete);
                return true;
            } catch (AssertionError | Exception e) {
                Log.e("NoteManager", "Error deleting note: " + e.getMessage());
                return false;
            }
        };
        return Futures.submit(task, MoreExecutors.directExecutor());
    }

    /**
     * <h2>updateNote</h2>
     * Update a note in the file system.
     * @param fileUri The URI of the note to update.
     * @param content The new content of the note.
     */
    public ListenableFuture<Void> updateNote(Uri fileUri, String content) {
        Callable<Void> task = () -> {
//            DocumentFile file = DocumentFile.fromTreeUri(context, fileUri);
            DocumentFile file = DocumentFile.fromSingleUri(context, fileUri);

            try {

                assert file != null;
                Log.d("NoteManager/UpdateNote/FileUri", "File URI: " + file.getUri());
                Log.d("NoteManager/UpdateNote/FileUri", "Content: " + content);
                ListenableFuture<Boolean> result = FileManager.write(file.getUri(), content, context);

                if(!result.get()){
                    throw new Exception("Error writing to file");
                }

            } catch (AssertionError | Exception e) {
                Log.e("NoteManager", "Error updating note: " + e.getMessage());
            }
            return null;
        };
        return Futures.submit(task, MoreExecutors.directExecutor());
    }

    /**
     * <h2>clearDatabase</h2>
     * <b>Just For Test DataBase</b>
     * Clear all notes from the database.
     */
    public ListenableFuture<Void> clearDatabase() {
        Callable<Void> task =  () -> {
            db.clearAllTables();
            return null;
        };

        return Futures.submit(task, MoreExecutors.directExecutor());
    }

    /**
     * <h2>getAllNotes</h2>
     */
    public ArrayList<DataEntityNote> getAllNotes() {
        try {
            return new ArrayList<>(daoNote.getAllNote().get());
        } catch (ExecutionException | InterruptedException e) {
            Log.e("NoteManager", "Error getting all notes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

}
