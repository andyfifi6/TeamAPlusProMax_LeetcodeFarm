package neu.finalPro.LeetcodeFarm.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import neu.finalPro.LeetcodeFarm.Constants;
import neu.finalPro.LeetcodeFarm.databinding.ActivityFriendListBinding;
import neu.finalPro.LeetcodeFarm.models.User;

public class FriendList extends AppCompatActivity {
    private ActivityFriendListBinding binding;
    private String currentUserId;
    List<String> friendIdList = new ArrayList<>();
    List<User> users = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent i = getIntent();
        String username = i.getStringExtra("username");
        currentUserId = i.getStringExtra("userId");
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.username.setText(username);
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


    private void getFriends(){
        loading(true);
        db.collection("friends")
                .whereEqualTo("userId",currentUserId )
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
                .whereEqualTo("friendId", currentUserId )
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
                            UserListener userListener = new UserListener() {
                                @Override
                                public void onUserClicked(User user) {
                                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                    intent.putExtra("friendName", user.getUsername());
                                    intent.putExtra("friendId", user.getId());
                                    intent.putExtra("userId", currentUserId);
                                    startActivity(intent);
                                }
                            };
                            UserAdapter usersAdapter = new UserAdapter(users, userListener);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("error","there is an error");
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

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


}