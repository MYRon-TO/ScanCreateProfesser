package model.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {dataEntityNote.class}, version = 1)
public abstract class dataBaseMain extends RoomDatabase {
    public abstract daoNote daoNote();
}
