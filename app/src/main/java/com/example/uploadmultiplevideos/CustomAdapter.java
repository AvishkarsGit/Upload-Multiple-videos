package com.example.uploadmultiplevideos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    ArrayList<String> status,files;
    Context context;

    public CustomAdapter( ArrayList<String> status, ArrayList<String> files, Context context) {
        this.status = status;
        this.files = files;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_select_row,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String filename = files.get(position);
        if (filename.length()>16){
            filename = filename.substring(0,16)+"...";
        }
        holder.tvTitle.setText(filename);
        String fileStatus = status.get(position);
        if (fileStatus.equals("loading")){
            holder.pgb.setVisibility(View.VISIBLE);
        }
        else {
            holder.pgb.setVisibility(View.GONE);
            holder.progress.setVisibility(View.VISIBLE);
            holder.progress.setImageResource(R.drawable.done);
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle;
        ImageView progress;
        ProgressBar pgb;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvVideoTitle);
            progress = itemView.findViewById(R.id.pgbImg);
            pgb = itemView.findViewById(R.id.pgb);
        }
    }
}
