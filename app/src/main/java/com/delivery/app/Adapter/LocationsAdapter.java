package com.delivery.app.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.delivery.app.Models.Locations;
import com.delivery.app.R;

import java.util.ArrayList;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {

    private static final String TAG = "LocationsAdapter";

    private ArrayList<Locations> listModels;
    private Context context;
    boolean[] selectedService;
    android.app.AlertDialog confirmation_dialogue;
    private LocationsListener locationsListener;
    private int item_position = 0;

    public LocationsAdapter(ArrayList<Locations> listModel, Context context) {
        this.listModels = listModel;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.locations_list_item, parent, false);
        return new ViewHolder(v);
    }

    public void setLocationsListener(LocationsListener locationsListener) {
        this.locationsListener = locationsListener;
    }

    public interface LocationsListener {
        void onCloseClick(Locations locations);

        void onSrcClick(Locations locations);

        void onDestClick(Locations locations);

        void onGoodsClick(Locations locations);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView srcTxt, destTxt, goodTxt, rec_name, rec_number;
        ImageView closeImg;
        LinearLayout lnr_helper;

        public ViewHolder(View itemView) {
            super(itemView);
            srcTxt = itemView.findViewById(R.id.src_txt);
            destTxt = itemView.findViewById(R.id.dest_txt);
            goodTxt = itemView.findViewById(R.id.good_txt);
            closeImg = itemView.findViewById(R.id.close_img);

            rec_name = itemView.findViewById(R.id.rec_name);
            rec_number = itemView.findViewById(R.id.rec_number);


            srcTxt.setOnClickListener(this);
            destTxt.setOnClickListener(this);
            goodTxt.setOnClickListener(this);
            closeImg.setOnClickListener(this);
            rec_name.setOnClickListener(this);
            rec_number.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (locationsListener != null) {
                final Locations locations = (Locations) v.getTag();
                if (v == closeImg) {
                    android.app.AlertDialog.Builder builder =
                            new android.app.AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    builder.setTitle(context.getResources().getString(R.string.app_name))
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage(context.getResources().getString(R.string.alert));
                    builder.setCancelable(false);
                    builder.setPositiveButton(
                            context.getResources().getString(R.string.yes),
                            (dialog, which) -> locationsListener.onCloseClick(locations)
                    );
                    builder.setNegativeButton(
                            context.getResources().getString(R.string.no),
                            (dialog, which) -> dialog.dismiss()
                    );
                    confirmation_dialogue = builder.create();
                    confirmation_dialogue.show();

                } else if (v == srcTxt)
                    locationsListener.onSrcClick(locations);
                else if (v == destTxt)
                    locationsListener.onDestClick(locations);
                else if (v == goodTxt)
                    showGoodsDialog(locations, "goods");
                else if (v == rec_name)
                    showGoodsDialog(locations, "name");
                else if (v == rec_number)
                    showGoodsDialog(locations, "number");

            }
        }
    }

    public void setListModels(ArrayList<Locations> listModels) {
        this.listModels = listModels;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Locations locations = listModels.get(position);
        item_position = position;
        if (position == 0)
            holder.closeImg.setVisibility(View.GONE);
        else
            holder.closeImg.setVisibility(View.VISIBLE);



        if (locations.getsAddress() != null && !locations.getsAddress().equalsIgnoreCase("null") &&
                locations.getsAddress().length() > 0) {
            holder.srcTxt.setText(locations.getsAddress());
        } else
            holder.srcTxt.setText("");


        if (locations.getdAddress() != null)
            holder.destTxt.setText(locations.getdAddress());
        else
            holder.destTxt.setText("");

        if (locations.getGoods() != null)
            holder.goodTxt.setText(locations.getGoods());
        else
            holder.goodTxt.setText("");

        if (locations.getReciver_name() != null) {
            holder.rec_name.setText(locations.getReciver_name());
        } else {
            holder.rec_name.setText("");
        }

        if (locations.getReciver_number() != null) {
            holder.rec_number.setText(locations.getReciver_number());
        } else {
            holder.rec_number.setText("");
        }

        holder.destTxt.setTag(locations);
        holder.srcTxt.setTag(locations);
        holder.goodTxt.setTag(locations);
        holder.closeImg.setTag(locations);
        holder.rec_name.setTag(locations);
        holder.rec_number.setTag(locations);
    }

    @Override
    public int getItemCount() {
        return listModels.size();
    }

    public void showGoodsDialog(final Locations locations, String box) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Recipient Details");
        alert.setIcon(R.mipmap.ic_launcher);

        View custom = LayoutInflater.from(context).inflate(R.layout.custom_edit, null);
        final EditText input = custom.findViewById(R.id.desc);

        final EditText inputName = custom.findViewById(R.id.rec_name);
        final EditText inputNumber = custom.findViewById(R.id.rec_number);
        final EditText inputGoods = custom.findViewById(R.id.good_txt);

        final EditText helper_count = custom.findViewById(R.id.helper_count);
        final CheckBox check_helper = custom.findViewById(R.id.check_helper);
        final LinearLayout lnr_helper = custom.findViewById(R.id.lnr_helper);

        //lnr_helper.setVisibility(item_position == 0 ? View.VISIBLE : View.GONE);

        check_helper.setOnCheckedChangeListener((compoundButton, b) -> {
            helper_count.setVisibility(b ? View.VISIBLE : View.GONE);
        });

        alert.setView(custom);
        alert.setPositiveButton(
                context.getResources().getString(R.string.ok),
                (dialog, whichButton) -> {

                    locations.setGoods(inputGoods.getText().toString());
                    locations.setHelper_count(helper_count.getText().toString());
                    locations.setReciver_name(inputName.getText().toString());
                    locations.setReciver_number(inputNumber.getText().toString());
                    locationsListener.onGoodsClick(locations);

                    dialog.cancel();

                }
        );
        alert.setNegativeButton(
                context.getResources().getString(R.string.cancel),
                (dialog, whichButton) -> dialog.cancel()
        );

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        alertDialog.getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        Button buttonbackground = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        buttonbackground.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

        Button buttonbackground1 = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonbackground1.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
    }
}
