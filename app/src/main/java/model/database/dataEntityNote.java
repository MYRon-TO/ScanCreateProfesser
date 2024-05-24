package model.database;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.OffsetDateTime;

@Entity(tableName = "note")
public class dataEntityNote {

    @PrimaryKey
    @ColumnInfo(name = "note_file_uri")
    private Uri noteFile;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "change_at")
    private OffsetDateTime changeAt;
}
