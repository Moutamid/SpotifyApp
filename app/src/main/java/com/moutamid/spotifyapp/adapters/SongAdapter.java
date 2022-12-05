package com.moutamid.spotifyapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.moutamid.spotifyapp.R;
import com.moutamid.spotifyapp.Song;
import com.moutamid.spotifyapp.models.ArtistModel;
import com.moutamid.spotifyapp.models.SongModel;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongVH> {
    Context context;
    ArrayList<SongModel> list;

    public SongAdapter(Context context, ArrayList<SongModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public SongVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.songs, parent, false);
        return new SongAdapter.SongVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongVH holder, int position) {
        SongModel model = list.get(holder.getAdapterPosition());

        Glide.with(context).load(model.getImage()).placeholder(R.drawable.music).into(holder.imageView);
        holder.type.setText("Type : " + model.getType());
        holder.name.setText(model.getName());
        String s = "";
        for (int i=0; i < model.getArtistList().size(); i++){
            if (i==0){
                s = s + model.getArtistList().get(i).name;
            } else {
                s = s + model.getArtistList().get(i).name + ", ";
            }
        }
        holder.artist.setText("Artists : " + s);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SongVH extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name, artist, type;
        public SongVH(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.name);
            artist = itemView.findViewById(R.id.artist);
            type = itemView.findViewById(R.id.type);
        }
    }
}
