package com.delivery.app.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.delivery.app.Activities.ChatActivity;
import com.delivery.app.Models.Chat;
import com.delivery.app.R;
import com.delivery.app.Utils.Utilities;

import java.util.List;

public class ChatMessageAdapter extends ArrayAdapter<Chat> {

    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1;
    private Activity context;

    public ChatMessageAdapter(Activity context, List<Chat> data) {
        super(context, R.layout.item_mine_message, data);
        this.context = context;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Chat item = getItem(position);

        assert item != null;
        if (item.getSender().equals(ChatActivity.sender)) return MY_MESSAGE;
        else return OTHER_MESSAGE;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        int viewType = getItemViewType(position);
        final Chat chat = getItem(position);
        if (viewType == MY_MESSAGE) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_message, parent, false);
            if (chat.getType().equals("text")) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_message, parent, false);
                TextView textView = convertView.findViewById(R.id.text);
                textView.setText(getItem(position).getText());
                TextView timestamp = convertView.findViewById(R.id.timestamp);
                timestamp.setText(String.valueOf(Utilities.getDisplayableTime(chat.getTimestamp())));
            } else if (chat.getType().equals("image")) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_image, parent, false);
                ImageView mine_image = convertView.findViewById(R.id.mine_image);
                TextView timestamp = convertView.findViewById(R.id.timestamp);
                timestamp.setText(String.valueOf(Utilities.getDisplayableTime(chat.getTimestamp())));
                Glide.with(context)
                        .load(chat.getUrl())
                        .apply(RequestOptions
                                .placeholderOf(R.drawable.ic_dummy_user)
                                .dontAnimate().error(R.drawable.ic_dummy_user))
                        .into(mine_image);
            }
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_message, parent, false);
            if (chat.getType().equals("text")) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_message, parent, false);
                TextView textView = convertView.findViewById(R.id.text);
                textView.setText(getItem(position).getText());
                TextView timestamp = convertView.findViewById(R.id.timestamp);
                timestamp.setText(String.valueOf(Utilities.getDisplayableTime(chat.getTimestamp())));
            } else if (chat.getType().equals("image")) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_image, parent, false);
                ImageView other_image = convertView.findViewById(R.id.other_image);
                TextView timestamp = convertView.findViewById(R.id.timestamp);
                timestamp.setText(String.valueOf(Utilities.getDisplayableTime(chat.getTimestamp())));
                Glide.with(context)
                        .load(chat.getUrl())
                        .apply(RequestOptions
                                .placeholderOf(R.drawable.ic_dummy_user)
                                .dontAnimate()
                                .error(R.drawable.ic_dummy_user))
                        .into(other_image);
            }
        }
        return convertView;
    }
}
