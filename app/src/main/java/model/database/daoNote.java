package model.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

@Dao
public interface daoNote {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public ListenableFuture<Integer> insertNotes(dataEntityNote... notes);

    @Delete
    public ListenableFuture<Integer> deleteNotes(dataEntityNote... notes);

    @Update
    public ListenableFuture<Integer> updateNotes(dataEntityNote... notes);

//    @Query("SELECT * FROM note")
//    public ListenableFuture<dataEntityNote> queryNote ();
    // TODO: 5/24/24 query 
}
