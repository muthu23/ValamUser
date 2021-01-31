package com.delivery.app.Activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.delivery.app.Adapter.ChatMessageAdapter;
import com.delivery.app.Models.Chat;
import com.delivery.app.R;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private ChatMessageAdapter mAdapter;

    public static String chatPath = null;
    ListView chatLv;
    EditText message;
    ImageView send;
    LinearLayout chatControlsLayout;

    private DatabaseReference myRef;
    public static String sender = "user";
    private String userId, providerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatLv = findViewById(R.id.chat_lv);
        message = findViewById(R.id.message);
        send = findViewById(R.id.send);
        chatControlsLayout = findViewById(R.id.chat_controls_layout);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            chatPath = extras.getString("request_id", null);
            userId = extras.getString("user_id", null);
            providerId = extras.getString("provider_id", null);
            initChatView(chatPath);
        }
        send.setOnClickListener(v -> {
            String myText = message.getText().toString();
            if (myText.length() > 0) sendMessage(myText);
        });
    }

    private void initChatView(String chatPath) {
        if (chatPath == null) return;

        message.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                String myText = message.getText().toString().trim();
                if (myText.length() > 0) sendMessage(myText);
                handled = true;
            }
            return handled;
        });

        mAdapter = new ChatMessageAdapter(this, new ArrayList<>());
        chatLv.setAdapter(mAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(chatPath)/*.child("chat")*/;
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                assert chat != null;
                if (chat.getSender() != null && chat.getRead() != null)
                    if (!chat.getSender().equals(sender) && chat.getRead() == 0) {
                        chat.setRead(1);
                        dataSnapshot.getRef().setValue(chat);
                    }
                mAdapter.add(chat);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sendMessage(String messageStr) {
        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setTimestamp(new Date().getTime());
        chat.setType("text");
        chat.setText(messageStr);
        chat.setRead(0);
        chat.setDriverId(Integer.valueOf(providerId));
        chat.setUserId(Integer.valueOf(userId));
        myRef.push().setValue(chat);
        message.setText("");
        /*Call<Object> call = RetrofitClient.getAPIClient().postChatItem("user", providerId, messageStr);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, retrofit2.Response<Object> response) {
                System.out.println("RRR o = " + response);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
            }
        });*/
    }
}