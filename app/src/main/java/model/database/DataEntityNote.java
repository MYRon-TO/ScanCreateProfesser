package model.database;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.time.OffsetDateTime;

@Entity(tableName = "note")
@TypeConverters(Converters.class)
public class DataEntityNote {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "note_file_uri")
    @NonNull
    private Uri noteFile;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "change_at")
    private OffsetDateTime changeAt;

    public DataEntityNote(@NonNull Uri noteFile, String title, OffsetDateTime changeAt) {
        this.noteFile = noteFile;
        this.title = title;
        this.changeAt = changeAt;
    }

    @NonNull
    public Uri getNoteFile() {
        return noteFile;
    }

    public String getTitle() {
        return title;
    }

    public OffsetDateTime getChangeAt() {
        return changeAt;
    }

    public void setNoteFile(@NonNull Uri noteFile) {
        this.noteFile = noteFile;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setChangeAt(OffsetDateTime changeAt) {
        this.changeAt = changeAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}


class Converters {

    @TypeConverter
    public static Uri uriFromString(String value) {
        return value == null ? null : Uri.parse(value);
    }

    @TypeConverter
    public static String stringFromUri(Uri uri) {
        return uri == null ? null : uri.toString();
    }

    @TypeConverter
    public static OffsetDateTime offsetDateTimeFromString(String value) {
        return value == null ? null : OffsetDateTime.parse(value);
    }

    @TypeConverter
    public static String stringFromOffsetDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.toString();
    }
}
