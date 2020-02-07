/*
Thanks to https://stackoverflow.com/a/40584425 (General adapter)
and https://stackoverflow.com/a/48959184 (updating data)
 */

package com.techbotbuilder.streamteamohio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techbotbuilder.streamteamohio.classifier.Recognition;

import java.util.List;
import java.util.Locale;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private List<Recognition> data;
    private String[] classes;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MyAdapter(Context context, List<Recognition> data){
        this.mInflater = LayoutInflater.from(context);
        classes = context.getResources().getStringArray(R.array.classes);
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.classification_result_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recognition recognition = data.get(position);
        holder.title.setText(recognition.getTitle());
        String format = "%s: %04.1f%%";
        int i0 = recognition.getIndexRanked(0);
        int i1 = recognition.getIndexRanked(1);
        int i2 = recognition.getIndexRanked(2);
        holder.res1.setText(String.format(Locale.getDefault(), format, classes[i0], 100*recognition.getConfidence(i0)));
        holder.res2.setText(String.format(Locale.getDefault(), format, classes[i1], 100*recognition.getConfidence(i1)));
        holder.res3.setText(String.format(Locale.getDefault(), format, classes[i2], 100*recognition.getConfidence(i2)));
        Glide.with(holder.thumb).load(recognition.getUri()).centerCrop().into(holder.thumb);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView title;
        TextView res1, res2, res3;
        FrameLayout thumbnail;
        ImageView thumb;
        ViewHolder(View itemView){
            super(itemView);
            title = itemView.findViewById(R.id.fileTitle);
            res1 = itemView.findViewById(R.id.result1);
            res2 = itemView.findViewById(R.id.result2);
            res3 = itemView.findViewById(R.id.result3);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            thumb = new ImageView(thumbnail.getContext());
            thumbnail.addView(thumb);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if (mClickListener != null) mClickListener.onItemClick(v, getAdapterPosition());
        }
    }


    public void setClickListener(ItemClickListener itemClickListener){
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void onItemClick(View v, int position);
    }

    public void remove(int d){
        data.remove(d);
        notifyItemRemoved(d);
    }
    public void removeAll(){
        data.clear();
        notifyDataSetChanged();
    }
    public Recognition getItem(int d){
        return data.get(d);
    }

    public int indexOf(Recognition r){
        return data.indexOf(r);
    }

    public void update(int d, Recognition r){
        data.set(d, r);
        notifyItemChanged(d);
    }
    public void add(Recognition r){
        data.add(0, r);
        notifyItemInserted(0);
    }

}