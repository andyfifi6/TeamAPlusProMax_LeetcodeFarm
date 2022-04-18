package neu.finalPro.LeetcodeFarm.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import neu.finalPro.LeetcodeFarm.Constants;
import neu.finalPro.LeetcodeFarm.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;

    private EditText inputName, inputEmail, inputPassword, inputConfirmPassword;
    private ProgressBar progressBar;
    private Button buttonSignUp;
    public static String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inputName = binding.inputName;
        inputEmail = binding.inputEmail;
        inputPassword = binding.inputPassword;
        inputConfirmPassword = binding.inputConfirmPassword;
        progressBar = binding.progressBar;
        buttonSignUp = binding.buttonSignUp;

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidSignUpDetails()){
                    signUp();
                }
            }
        });
    }


    private Boolean isValidSignUpDetails(){

        if (inputName.getText().toString().trim().isEmpty()){
            showToast("Enter name");
            return false;
        } else if (inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        } else if (inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        } else if (inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Confirm your password");
            return false;
        } else if (!inputPassword.getText().toString().equals(inputPassword.getText().toString())){
            showToast("Password & confirm password must be same");
            return false;
        } else {
            return true;
        }
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_USERNAME, inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png");
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    currentUserId = documentReference.getId();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("userId", documentReference.getId());
                    intent.putExtra("username", inputName.getText().toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void loading(Boolean isLoading){
        if (isLoading) {
            buttonSignUp.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            buttonSignUp.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

}