package model.database;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.time.OffsetDateTime;


@Entity(tableName = "note")
@TypeConverters(Converters.class)
public class DataEntityNote {

    @PrimaryKey
    @ColumnInfo(name = "note_file_uri")
    @NonNull
    private Uri noteFile;
    @NonNull
    private String title;

    public DataEntityNote(@NonNull Uri noteFile, @NonNull String title) {
        this.noteFile = noteFile;
        this.title = title;
    }

    @NonNull
    public Uri getNoteFile() {
        return noteFile;
    }

    public void setNoteFile(@NonNull Uri noteFile) {
        this.noteFile = noteFile;
    }

    @NonNull
    public String getTitle(){
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
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
