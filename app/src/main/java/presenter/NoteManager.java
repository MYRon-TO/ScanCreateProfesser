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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import model.database.DaoNote;
import model.database.DataBaseMain;
import model.database.DataEntityNote;
import model.filemanager.FileManager;
import model.preference.PreferenceManager;


public class NoteManager {
    private static volatile NoteManager instance = null;

    private Context context;

    public static NoteManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("NoteManager is not initialized");
        }
        return instance;
    }

    public static NoteManager init(Context context) {
        if (instance == null) {
            synchronized (NoteManager.class) {
                instance = new NoteManager(context);
            }
        }
        return instance;
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
                daoNote.insertNotes(new DataEntityNote(fileUri));

                return fileUri;
            } catch (AssertionError | Exception e) {
                Log.e("NoteManager", "Error adding note: " + e.getMessage());
                return null;
            }
        };

        return Futures.submit(task, MoreExecutors.directExecutor());
    }

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
                daoNote.deleteNotes(new DataEntityNote(fileUri));
                return true;
            } catch (AssertionError | Exception e) {
                Log.e("NoteManager", "Error deleting note: " + e.getMessage());
                return false;
            }
        };
        return Futures.submit(task, MoreExecutors.directExecutor());
    }

    public ListenableFuture<Void> updateNote(Uri fileUri, String content) {
        Callable<Void> task = () -> {
//            DocumentFile file = DocumentFile.fromTreeUri(context, fileUri);
            DocumentFile file = DocumentFile.fromSingleUri(context, fileUri);

            try {
                assert file != null;

                Log.d("NoteManager/UpdateNote/FileUri", "File URI: " + file.getUri());
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

    public ListenableFuture<Void> clearDatabase() {
        Callable<Void> task =  () -> {
            db.clearAllTables();
            return null;
        };

        return Futures.submit(task, MoreExecutors.directExecutor());
    }

}
