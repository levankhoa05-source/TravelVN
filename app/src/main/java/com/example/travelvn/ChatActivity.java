package com.example.travelvn;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private List<Message> chatList;
    private String currentUserId;
    private String receiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUserId = LocalStore.getCurrentUser(this);
        if (currentUserId == null) {
            currentUserId = "guest@travelvn.local";
        }
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        if (receiverId == null || receiverId.isEmpty()) {
            receiverId = "support@travelvn.com";
        }
        String receiverName = getIntent().getStringExtra("RECEIVER_NAME");
        if (receiverName == null || receiverName.isEmpty()) {
            receiverName = getString(R.string.support_display_name);
        }

        ((TextView) findViewById(R.id.tvChatTitle)).setText(getString(R.string.chat_with_prefix, receiverName));

        rvChat = findViewById(R.id.rvChat);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList, currentUserId);
        rvChat.setAdapter(adapter);

        refreshChat();

        EditText etInput = findViewById(R.id.etMessageInput);
        findViewById(R.id.btnSendMessage).setOnClickListener(v -> {
            String txt = etInput.getText().toString();
            if (!txt.isEmpty()) {
                Message msg = new Message(currentUserId, receiverId, txt);
                LocalStore.sendMessage(this, msg);
                etInput.setText("");
                refreshChat();
                
                // Simple auto-reply mock
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                     Message reply = new Message(receiverId, currentUserId, "Chào bạn! Nhà xe đã nhận thông tin và sẽ phản hồi sớm.");
                     LocalStore.sendMessage(this, reply);
                     refreshChat();
                }, 1000);
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void refreshChat() {
        chatList.clear();
        for (Message m : LocalStore.getMessages()) {
            if ((m.from.equals(currentUserId) && m.to.equals(receiverId)) ||
                (m.from.equals(receiverId) && m.to.equals(currentUserId))) {
                chatList.add(m);
            }
        }
        adapter.notifyDataSetChanged();
        rvChat.scrollToPosition(chatList.size() - 1);
    }

    class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
        List<Message> list;
        String curId;
        ChatAdapter(List<Message> list, String curId) { this.list = list; this.curId = curId; }

        @Override public int getItemViewType(int pos) {
            return list.get(pos).from.equals(curId) ? 1 : 2;
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int res = viewType == 1 ? R.layout.item_chat_sent : R.layout.item_chat_received;
            return new VH(LayoutInflater.from(parent.getContext()).inflate(res, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.text.setText(list.get(position).content);
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView text;
            VH(View v) { super(v); text = v.findViewById(R.id.tvChatText); }
        }
    }
}
