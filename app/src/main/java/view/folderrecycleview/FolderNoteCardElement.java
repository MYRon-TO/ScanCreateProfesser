package view.folderrecycleview;

import android.net.Uri;

public class FolderNoteCardElement {
    public CharSequence noteTitle;
    public CharSequence noteContent;
    public Uri fileUri;

    public FolderNoteCardElement(CharSequence title, CharSequence text, Uri fileUri) {
        noteTitle = title;
        noteContent = text;
        this.fileUri = fileUri;
    }
}