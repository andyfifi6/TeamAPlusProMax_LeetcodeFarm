package neu.finalPro.LeetcodeFarm.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import neu.finalPro.LeetcodeFarm.databinding.ActivityChatBinding;
import neu.finalPro.LeetcodeFarm.models.ChatMessage;
import neu.finalPro.LeetcodeFarm.models.User;
import neu.finalPro.LeetcodeFarm.note.CreateNoteActivity;
import neu.finalPro.LeetcodeFarm.note.entities.Note;
import neu.finalPro.LeetcodeFarm.utility.Constants;
import neu.finalPro.LeetcodeFarm.utility.PreferenceManager;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private String receiverId, receiverName, userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        userId = preferenceManager.getString(Constants.KEY_USER_ID);
        receiverId = getIntent().getStringExtra("friendId");
        receiverName = getIntent().getStringExtra("friendName");
        init();
        listenMessages();
    }

    private void init(){
        binding.inputMessage.setVisibility(View.GONE);
        binding.layoutSend.setVisibility(View.GONE);
        chatMessages = new ArrayList<>();
        binding.textName.setText(receiverName);
        ItemListener listener = new ItemListener() {
            @Override
            public void onUserClicked(User user) {

            }

            @Override
            public void onChatClicked(ChatMessage chatMessage) {
                Note clickedNote = new Note();
                clickedNote.setTitle(chatMessage.noteTitle);
                clickedNote.setSubtitle(chatMessage.noteSubtitle);
                clickedNote.setNoteText(chatMessage.noteText);
                clickedNote.setImagePath(chatMessage.noteImagePath);
                clickedNote.setDateTime(chatMessage.noteDateTime);

                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                intent.putExtra("ShareMode", true);
                intent.putExtra("note", clickedNote);
                startActivity(intent);
            }
        };
        chatAdapter = new ChatAdapter(
                chatMessages,
                userId,
                listener
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> {
            Intent prevPage = new Intent(getApplicationContext(), FriendList.class);
            prevPage.putExtra("userId", userId);
            startActivity(prevPage);
        });
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString("senderId");
                    chatMessage.receiverId = documentChange.getDocument().getString("receiverId");
                    chatMessage.content = documentChange.getDocument().getString("message");
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate("timestamp"));
                    chatMessage.dateObject = documentChange.getDocument().getDate("timestamp");
                    // get note information
                    chatMessage.noteTitle = documentChange.getDocument().getString("noteTitle");
                    chatMessage.noteSubtitle = documentChange.getDocument().getString("noteSubtitle");
                    chatMessage.noteImagePath = documentChange.getDocument().getString("noteImagePath");
                    chatMessage.noteDateTime = documentChange.getDocument().getString("noteDateTime");
                    chatMessage.noteText = documentChange.getDocument().getString("noteText");

                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    };

    private void listenMessages(){
        database.collection("shareNotes")
                .whereEqualTo("senderId", userId)
                .whereEqualTo("receiverId", receiverId)
                .addSnapshotListener(eventListener);
        database.collection("shareNotes")
                .whereEqualTo("senderId", receiverId)
                .whereEqualTo("receiverId", userId)
                .addSnapshotListener(eventListener);
    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put("senderId", userId);
        message.put("receiverId", receiverId);
        message.put("message", binding.inputMessage.getText().toString());
        message.put("timestamp", new Date());
        database.collection("chats").add(message);
        binding.inputMessage.setText(null);
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}