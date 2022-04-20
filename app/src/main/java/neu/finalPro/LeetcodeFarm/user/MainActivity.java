package neu.finalPro.LeetcodeFarm.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import neu.finalPro.LeetcodeFarm.R;
import neu.finalPro.LeetcodeFarm.databinding.ActivityMainBinding;
import neu.finalPro.LeetcodeFarm.note.NoteActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private String userId, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");
        setListeners();
    }

    private void setListeners() {
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
        binding.friendList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FriendList.class);
                intent.putExtra("userId", userId);
                intent.putExtra("username", username);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }



}