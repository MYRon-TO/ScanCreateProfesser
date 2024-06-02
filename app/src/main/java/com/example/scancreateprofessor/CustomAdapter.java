package com.example.scancreateprofessor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private int mItemLayoutID;
    private ArrayList<DataElement> mAdapterData;

    public CustomAdapter(int resid, ArrayList<DataElement> data){
        mItemLayoutID = resid;
        mAdapterData = data;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextViewText;
        private TextView mTextViewTitle;
        public MyViewHolder(View v) {
            super(v);

            mTextViewText = v.findViewById(R.id.title);
            mTextViewTitle = v.findViewById(R.id.text);
        }

        public TextView getTextViewTitle() {
            return mTextViewTitle;
        }

        public TextView getTextViewText() {
            return mTextViewText;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(mItemLayoutID, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.getTextViewText().setText(mAdapterData.get(position).Text);
        holder.getTextViewTitle().setText(mAdapterData.get(position).Title);
    }

    @Override
    public int getItemCount() {
        return mAdapterData.size();
    }
}
