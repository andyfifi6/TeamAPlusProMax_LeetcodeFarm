package neu.finalPro.LeetcodeFarm.user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import neu.finalPro.LeetcodeFarm.databinding.ActivityAddFriendBinding;

public class AddFriendActivity extends AppCompatActivity {
    private ActivityAddFriendBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    }
}