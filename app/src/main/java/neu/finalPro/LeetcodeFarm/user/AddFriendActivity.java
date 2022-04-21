package neu.finalPro.LeetcodeFarm.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import neu.finalPro.LeetcodeFarm.databinding.ActivityAddFriendBinding;
import neu.finalPro.LeetcodeFarm.models.ChatMessage;
import neu.finalPro.LeetcodeFarm.models.User;
import neu.finalPro.LeetcodeFarm.utility.Constants;
import neu.finalPro.LeetcodeFarm.utility.PreferenceManager;

public class AddFriendActivity extends AppCompatActivity {
    private ActivityAddFriendBinding binding;
    private List<User> users = new ArrayList<>();
    private List<String> friendIdList = new ArrayList<>();
    private String userId;
    private PreferenceManager preferenceManager;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        userId = preferenceManager.getString(Constants.KEY_USER_ID);

        friendIdList = getIntent().getStringArrayListExtra("friendList");
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("users").
                        whereEqualTo("email", binding.inputEmail.getText().toString())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                        User user = new User(queryDocumentSnapshot.get("username").toString(), queryDocumentSnapshot.get("email").toString(),queryDocumentSnapshot.getId());
                                        users.add(user);
                                    }

                                    if(users.size() > 0) {
                                        ItemListener itemListener = new ItemListener() {
                                            @Override
                                            public void onUserClicked(User user) {
                                                String currentId = user.getId();
                                                for(String id : friendIdList) {
                                                    if(currentId.equals(id)) {
                                                        showToast("You are friends!");
                                                        return;
                                                    }
                                                }
                                                HashMap<String, Object> friendRecord = new HashMap<>();
                                                friendRecord.put("userId", userId);
                                                friendRecord.put("friendId", currentId);
                                                db.collection("friends").add(friendRecord).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        showToast("Successfully added!");
                                                        Intent intent = new Intent(getApplicationContext(), FriendList.class);
                                                        startActivity(intent);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onChatClicked(ChatMessage chatMessage) {

                                            }
                                        };
                                        UserAdapter usersAdapter = new UserAdapter(users, itemListener);
                                        binding.usersRecyclerView.setAdapter(usersAdapter);
                                        binding.usersRecyclerView.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });
            }
        });

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}