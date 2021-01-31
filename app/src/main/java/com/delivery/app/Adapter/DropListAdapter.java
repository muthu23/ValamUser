package com.delivery.app.Adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.delivery.app.Models.Locations;
import com.delivery.app.R;

import java.util.ArrayList;

public class DropListAdapter extends RecyclerView.Adapter<DropListAdapter.ViewHolder> {

    private ArrayList<Locations> dropList;

    public DropListAdapter(ArrayList<Locations> dropList) {
        this.dropList = dropList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drop_list_item, parent, false);
        return new ViewHolder(v);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDropLocation;
        View viewMarkerBottom, viewDivider;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDropLocation = itemView.findViewById(R.id.tvDropLocation);
            viewMarkerBottom = itemView.findViewById(R.id.viewMarkerBottom);
            viewDivider = itemView.findViewById(R.id.viewDivider);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.tvDropLocation.setText(dropList.get(position).getdAddress());
        if (position == dropList.size() - 1) {
            holder.viewMarkerBottom.setVisibility(View.GONE);
            holder.viewDivider.setVisibility(View.GONE);
        } else {
            holder.viewMarkerBottom.setVisibility(View.VISIBLE);
            holder.viewDivider.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return dropList.size();
    }
}