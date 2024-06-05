package view.folderrecycleview;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scancreateprofessor.NoteActivity;
import com.example.scancreateprofessor.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import model.UriStringConverters;

public class FolderNoteCardAdapter extends RecyclerView.Adapter<FolderNoteCardAdapter.FolderNoteCardViewHolder> {

    private final int folderNoteCardRowID;
    private final ArrayList<FolderNoteCardElement> folderNoteCardData;
    private final Context context;
    private static final String TAG = "FolderNoteCardAdapter";

    public FolderNoteCardAdapter(int id, ArrayList<FolderNoteCardElement> data, Context context){
        folderNoteCardRowID = id;
        folderNoteCardData = data;
        this.context = context;
    }

    public static class FolderNoteCardViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleFolderNoteCardRowItem;
        private final TextView contentFolderNoteCardRowItem;
        private final MaterialCardView cardFolderNoteCardRowItem;
        public FolderNoteCardViewHolder(View v) {
            super(v);
            titleFolderNoteCardRowItem = v.findViewById(R.id.title_folder_note_card_row_item);
            contentFolderNoteCardRowItem = v.findViewById(R.id.content_folder_note_card_row_item);
            cardFolderNoteCardRowItem = v.findViewById(R.id.card_folder_note_card_row_item);
        }

        public void setTitle(CharSequence title){
            titleFolderNoteCardRowItem.setText(title);
        }

        public void setContent(CharSequence content){
            contentFolderNoteCardRowItem.setText(content);
        }

        public MaterialCardView getCardFolderNoteCardRowItem(){
            return cardFolderNoteCardRowItem;
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
        holder.setContent(folderNoteCardData.get(position).noteContent);
        holder.setTitle(folderNoteCardData.get(position).noteTitle);
        holder.getCardFolderNoteCardRowItem().setOnClickListener(
                v -> {
//                    Intent intent = new Intent(FolderActivity., NoteActivity.class);
                    Intent intent = new Intent(context, NoteActivity.class);
                    Log.d(TAG+"/intent", UriStringConverters.stringFromUri(folderNoteCardData.get(position).fileUri));
                    intent.putExtra("Title", folderNoteCardData.get(position).noteTitle);
                    intent.putExtra("FileUri", UriStringConverters.stringFromUri(folderNoteCardData.get(position).fileUri));

                    context.startActivity(intent);
                }
        );
    }

    @Override
    public int getItemCount() {
        return folderNoteCardData.size();
    }
}
