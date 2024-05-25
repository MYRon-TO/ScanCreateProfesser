package model.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

@Dao
public interface DaoNote {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public ListenableFuture<Void> insertNotes(DataEntityNote... notes);

    @Delete
    public ListenableFuture<Integer> deleteNotes(DataEntityNote... notes);

    @Update
    public ListenableFuture<Integer> updateNotes(DataEntityNote... notes);

//    @Query("SELECT * FROM note")
//    public ListenableFuture<dataEntityNote> queryNote ();
    // TODO: 5/24/24 query 
}
