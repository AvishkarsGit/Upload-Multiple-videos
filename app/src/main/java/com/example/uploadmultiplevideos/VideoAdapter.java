package com.example.uploadmultiplevideos;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {


    ArrayList<DatabaseModel> modelArrayList;
    Context context;

    public VideoAdapter(ArrayList<DatabaseModel> modelArrayList, Context context) {
        this.modelArrayList = modelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_show_row,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.tvTitle.setText(modelArrayList.get(position).getVideoName());
        String videoUrl = modelArrayList.get(position).getVideoUrl();
        MediaController mc = new MediaController(context);
        mc.setAnchorView(holder.videoView);



        Uri videoUri = Uri.parse(videoUrl);
        holder.videoView.setMediaController(mc);
        holder.videoView.setVideoURI(videoUri);
        holder.videoView.requestFocus();

        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                holder.progressBar.setVisibility(View.GONE);
                mp.start();
            }
        });

        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle;
        VideoView videoView;
        ProgressBar progressBar;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.vvRow);
            tvTitle = itemView.findViewById(R.id.tvTitle);

            progressBar = itemView.findViewById(R.id.pgb);
        }
    }
}
