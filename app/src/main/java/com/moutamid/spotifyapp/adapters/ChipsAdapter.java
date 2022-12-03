package com.moutamid.spotifyapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.spotifyapp.ArtistActivity;
import com.moutamid.spotifyapp.R;
import com.moutamid.spotifyapp.listners.ChipsClick;
import com.moutamid.spotifyapp.models.ArtistModel;

import java.util.ArrayList;

public class ChipsAdapter extends RecyclerView.Adapter<ChipsAdapter.ChipsVH> {
    Context context;
    ArrayList<ArtistModel> chips;
    ChipsClick chipsClick;
    final int limit = 4;

    public ChipsAdapter(Context context, ArrayList<ArtistModel> chips, ChipsClick chipsClick) {
        this.context = context;
        this.chips = chips;
        this.chipsClick = chipsClick;
    }

    @NonNull
    @Override
    public ChipsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chips, parent, false);
        return new ChipsVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipsVH holder, int position) {
        holder.name.setText(chips.get(holder.getAdapterPosition()).getName());
        holder.itemView.setOnClickListener(v -> chipsClick.onClick(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        if (chips.size() >= limit){
            return limit;
        }
        return chips.size();
    }


    public class ChipsVH extends RecyclerView.ViewHolder{
        TextView name;
        public ChipsVH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }

}
