package model.database;

import android.net.Uri;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface DaoNote {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public ListenableFuture<Void> insertNotes(DataEntityNote... notes);

    @Delete
    public ListenableFuture<Integer> deleteNotes(DataEntityNote... notes);

    @Update
    public ListenableFuture<Integer> updateNotes(DataEntityNote... notes);

    @Query("SELECT * FROM note")
    public ListenableFuture<List<DataEntityNote>> getAllNote ();

    @Query("SELECT * FROM note WHERE note_file_uri = :noteFile")
    public ListenableFuture<DataEntityNote> getNoteByUri(String noteFile);

}
