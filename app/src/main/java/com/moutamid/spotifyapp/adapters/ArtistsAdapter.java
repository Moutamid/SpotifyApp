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
import com.moutamid.spotifyapp.listners.ArtistClickListen;
import com.moutamid.spotifyapp.models.ArtistModel;

import java.util.ArrayList;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ArtistVH> {

    Context context;
    ArrayList<ArtistModel> list;
    ArtistClickListen clickListen;

    public ArtistsAdapter(Context context, ArrayList<ArtistModel> list, ArtistClickListen clickListen) {
        this.context = context;
        this.list = list;
        this.clickListen = clickListen;
    }

    @NonNull
    @Override
    public ArtistVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.artist_card, parent, false);
        return new ArtistVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistVH holder, int position) {
        ArtistModel model = list.get(holder.getAdapterPosition());

        Glide.with(context).load(model.getImage()).placeholder(R.drawable.profile_icon).into(holder.imageView);
        holder.type.setText("Type : " + model.getType());
        holder.name.setText(model.getName());
        holder.follower.setText("Followers : " + model.getFollowers());

        holder.itemView.setOnClickListener(v -> clickListen.onClick(list.get(holder.getAdapterPosition())));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ArtistVH extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name, follower, type;

        public ArtistVH(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.name);
            follower = itemView.findViewById(R.id.followers);
            type = itemView.findViewById(R.id.type);

        }
    }
}
