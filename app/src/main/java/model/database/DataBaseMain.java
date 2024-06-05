package model.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DataEntityNote.class}, version = 2, exportSchema = false)
public abstract class DataBaseMain extends RoomDatabase {
    public abstract DaoNote daoNote();
}
