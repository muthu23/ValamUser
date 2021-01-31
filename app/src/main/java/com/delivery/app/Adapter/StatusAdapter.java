package com.delivery.app.Adapter;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.delivery.app.Models.TripStatus;
import com.delivery.app.R;
import com.delivery.app.Utils.MyButton;
import com.delivery.app.Utils.MyTextView;

import java.util.ArrayList;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    public StatuAdapterListner statusadapterListener;
    private ArrayList<TripStatus> listModels;
    private Activity activity;


    public StatusAdapter(ArrayList<TripStatus> listModel, Activity activity) {
        this.listModels = listModel;
        this.activity = activity;
    }

    public void setStatusadapterListener(StatuAdapterListner statusadapterListener) {
        this.statusadapterListener = statusadapterListener;
    }

    public void setListModels(ArrayList<TripStatus> listModels) {
        this.listModels = listModels;
    }

    @Override
    public StatusAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_status, parent, false);
        return new StatusAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        TripStatus status = listModels.get(position);

        if (status.getstatus() != null) {
            if (status.getstatus().equalsIgnoreCase("SEARCHING")) {
                holder.status.setText(R.string.yet_pickup);
                holder.status_img.setImageResource(R.drawable.radio_btn_grey);
            } else if (status.getstatus().equalsIgnoreCase("STARTED") ||
                    status.getstatus().equalsIgnoreCase("DROPPED")) {
                holder.status.setText(R.string.pickup);
                holder.status_img.setImageResource(R.drawable.radio_btn_green);
            } else if (status.getstatus().equalsIgnoreCase("COMPLETED")) {
                holder.status.setText(R.string.order_delivered);
                holder.status_img.setImageResource(R.drawable.radio_btn_red);
            } else {
                holder.status.setText("");
            }
        }
        if (status.getdeliveryAddress() != null) {
            holder.destination_address.setText(status.getdeliveryAddress());
        } else {
            holder.destination_address.setText("");
        }
        if (status.getcomments() != null) {
            holder.comments.setText(status.getcomments());
        } else {
            holder.comments.setText("");
        }
        holder.destination_address.setTag(status);
        holder.status.setTag(status);
        holder.comments.setTag(status);
        holder.track_btn.setTag(status);
        /*holder.imgGotoPhoto.setVisibility(status.getAfterImage() != null &&
                !status.getAfterImage().equals("") &&
                !status.getAfterImage().equals("null") ? View.VISIBLE : View.GONE);
        holder.imgGotoPhoto.setOnClickListener(v -> {
            if (status.getAfterImage() != null &&
                    !status.getAfterImage().equals(""))
                openPhoto(status.getAfterImage());
        });*/
    }

    @Override
    public int getItemCount() {
        return listModels.size();
    }


    public interface StatuAdapterListner {

        void onTrackbtn(TripStatus statusflow);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        MyTextView destination_address, comments, status;
        MyButton track_btn;
        ImageView status_img/*, imgGotoPhoto*/;

        public ViewHolder(View itemView) {
            super(itemView);
            destination_address = itemView.findViewById(R.id.destination_address);
            comments = itemView.findViewById(R.id.comments);
            status = itemView.findViewById(R.id.status);
            track_btn = itemView.findViewById(R.id.track_btn);
            status_img = itemView.findViewById(R.id.status_img);
//            imgGotoPhoto = itemView.findViewById(R.id.imgGotoPhoto);
            track_btn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            TripStatus status = (TripStatus) view.getTag();
            if (statusadapterListener != null) {
                if (view == track_btn) {
                    statusadapterListener.onTrackbtn(status);
                }
            }
        }
    }

    /*private void openPhoto(String url) {
        android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_open_photo, null);
        dialogBuilder.setView(dialogView);
        final ImageView ivPhoto = dialogView.findViewById(R.id.ivPhoto);
        final TextView tvClose = dialogView.findViewById(R.id.tvClose);
        android.support.v7.app.AlertDialog alertDialog = dialogBuilder.create();
        Glide.with(activity).load(url)
                .apply(new RequestOptions().placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)).into(ivPhoto);
        tvClose.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }*/
}