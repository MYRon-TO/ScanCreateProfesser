package com.example.scancreateprofessor.folderrecycleview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scancreateprofessor.R;

import java.util.ArrayList;

public class FolderNoteCardAdapter extends RecyclerView.Adapter<FolderNoteCardAdapter.FolderNoteCardViewHolder> {

    private final int folderNoteCardRowID;
    private final ArrayList<FolderNoteCardElement> folderNoteCardData;

    public FolderNoteCardAdapter(int id, ArrayList<FolderNoteCardElement> data){
        folderNoteCardRowID = id;
        folderNoteCardData = data;
    }

    public static class FolderNoteCardViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleFolderNoteCardRowItem;
        private final TextView contentFolderNoteCardRowItem;
        public FolderNoteCardViewHolder(View v) {
            super(v);
            titleFolderNoteCardRowItem = v.findViewById(R.id.title_folder_note_card_row_item);
            contentFolderNoteCardRowItem = v.findViewById(R.id.content_folder_note_card_row_item);
        }

        public void setTitle(CharSequence title){
            titleFolderNoteCardRowItem.setText(title);
        }

        public void setContent(CharSequence content){
            contentFolderNoteCardRowItem.setText(content);
        }
    }

    @NonNull
    @Override
    public FolderNoteCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(folderNoteCardRowID, parent, false);

        return new FolderNoteCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderNoteCardViewHolder holder, int position) {
        holder.setContent(folderNoteCardData.get(position).NoteContent);
        holder.setTitle(folderNoteCardData.get(position).NoteTitle);
    }

    @Override
    public int getItemCount() {
        return folderNoteCardData.size();
    }
}
