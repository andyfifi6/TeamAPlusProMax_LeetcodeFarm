package neu.finalPro.LeetcodeFarm.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import neu.finalPro.LeetcodeFarm.utility.Constants;
import neu.finalPro.LeetcodeFarm.databinding.ActivityFriendListBinding;
import neu.finalPro.LeetcodeFarm.models.ChatMessage;
import neu.finalPro.LeetcodeFarm.models.User;
import neu.finalPro.LeetcodeFarm.note.entities.Note;
import neu.finalPro.LeetcodeFarm.utility.PreferenceManager;

public class FriendList extends AppCompatActivity {
    private ActivityFriendListBinding binding;
    private String userId;
    private boolean shareMode;
    private PreferenceManager preferenceManager;
    List<String> friendIdList = new ArrayList<>();
    List<User> users = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        userId = preferenceManager.getString(Constants.KEY_USER_ID);
        shareMode = getIntent().getBooleanExtra("shareMode",false);
        if(shareMode) {
            binding.newFriend.setVisibility(View.GONE);
        }
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.newFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
                intent.putStringArrayListExtra("friendList", (ArrayList<String>) friendIdList);
                startActivity(intent);
            }
        });
        binding.username.setText(preferenceManager.getString(Constants.KEY_USERNAME));
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        initialItemData(savedInstanceState);
    }

    private void initialItemData(Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.NUMBER_OF_ITEMS)) {
            if (users == null || users.size() == 0) {

                int size = savedInstanceState.getInt(Constants.NUMBER_OF_ITEMS);

                for (int i = 0; i < size; i++) {
                    String username = savedInstanceState.getString(Constants.KEY_OF_INSTANCE + i + "1");
                    String userEmail = savedInstanceState.getString(Constants.KEY_OF_INSTANCE + i + "2");
                    String userId= savedInstanceState.getString(Constants.KEY_OF_INSTANCE + i + "3");

                    User user = new User(username, userEmail, userId);
                    users.add(user);
                }
            }
        }
        else {
            getFriends();

        }
    }

    @Override
    public void onRestart() {  // After a pause OR at startup
        super.onRestart();
        users.clear();
        friendIdList.clear();
        getFriends();
    }


    private void getFriends(){
        loading(true);
        db.collection("friends")
                .whereEqualTo("userId",userId )
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            String friendId = queryDocumentSnapshot.get("friendId").toString();
                            friendIdList.add(friendId);
                        }
                    } else {
                        Log.e("error","there is an error");
                    }
                });

        db.collection("friends")
                .whereEqualTo("friendId", userId )
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            String friendId = queryDocumentSnapshot.get("userId").toString();
                            friendIdList.add(friendId);
                        }
                        if(friendIdList.size() > 0 ){
                            loading(false);
                            getUser();
                        }
                    } else {
                        Log.e("error","there is an error");
                    }
                });


    }

    private void getUser() {
        db.collection("users")
                .whereIn(FieldPath.documentId(),  friendIdList)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            users.add(new User(queryDocumentSnapshot.get("username").toString(), queryDocumentSnapshot.get("email").toString(), queryDocumentSnapshot.getId()));
                        }

                        if(users.size() > 0) {
                            ItemListener itemListener;
                            if(shareMode) {
                                itemListener = new ItemListener() {
                                    @Override
                                    public void onUserClicked(User user) {
                                        Note noteToShare = (Note) getIntent().getSerializableExtra("note");
                                        addShareNoteToDB(noteToShare,userId, user.getId());
                                        showToast("Successfully share the note with " + user.getUsername());
                                    }

                                    @Override
                                    public void onChatClicked(ChatMessage chatMessage) {

                                    }
                                };
                            } else {
                                itemListener = new ItemListener() {
                                    @Override
                                    public void onUserClicked(User user) {
                                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                        intent.putExtra("friendName", user.getUsername());
                                        intent.putExtra("friendId", user.getId());
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onChatClicked(ChatMessage chatMessage) {

                                    }
                                };
                            }
                            UserAdapter usersAdapter = new UserAdapter(users, itemListener);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("error","there is an error");
                    }
                });
    }

    private void addShareNoteToDB(Note note, String userId, String friendId) {
        HashMap<String, Object> message = new HashMap<>();
        message.put("senderId", userId);
        message.put("receiverId", friendId);
        message.put("message", "Note shared successfully! Click to view it");
        message.put("timestamp", new Date());
        // information about notes
        message.put("noteTitle", note.getTitle());
        message.put("noteSubtitle", note.getSubtitle());
        message.put("noteText", note.getNoteText());
        message.put("noteDateTime", note.getDateTime());
        message.put("noteImagePath", note.getImagePath());
        db.collection("shareNotes").add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("FriendList", "successfully add shared note to db");

            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        int size = users == null ? 0 : users.size();
        outState.putInt(Constants.NUMBER_OF_ITEMS, size);

        for (int i = 0; i < size; i++) {

            outState.putString(Constants.KEY_OF_INSTANCE + i + "1", users.get(i).getUsername());

            outState.putString(Constants.KEY_OF_INSTANCE + i + "2", users.get(i).getUserEmail());

            outState.putString(Constants.KEY_OF_INSTANCE + i + "3", users.get(i).getId());
        }
        super.onSaveInstanceState(outState);

    }


    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


}