package neu.finalPro.LeetcodeFarm.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import neu.finalPro.LeetcodeFarm.note.NoteActivity;
import neu.finalPro.LeetcodeFarm.utility.Constants;
import neu.finalPro.LeetcodeFarm.R;
import neu.finalPro.LeetcodeFarm.databinding.ActivityGrowthBinding;
import neu.finalPro.LeetcodeFarm.utility.PreferenceManager;

public class GrowthActivity extends AppCompatActivity {

    private ActivityGrowthBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PreferenceManager preferenceManager;
    private Long lastCheckInTime;
    private int currentMileStone;
    private int currentCheckInDateCount;
    private int progress;

    private String username;
    private String userId;
    private String userEmail;

    private final static Integer TIME_INTERVAL = 1000;
    private final static Integer MILESTONE_INTERVAL = 5;
    private final static String CHECKIN_DATE_COUNT = "CHECKIN_DATE_COUNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGrowthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();

        //Set up Initial Data
        preferenceManager = new PreferenceManager(getApplicationContext());
        username = preferenceManager.getString(Constants.KEY_USERNAME);
        userId = preferenceManager.getString(Constants.KEY_USER_ID);
        userEmail = preferenceManager.getString(Constants.KEY_EMAIL);

//        username = getIntent().getStringExtra("username");
//        userId = getIntent().getStringExtra("userId");
//        userEmail = getIntent().getStringExtra("userEmail");
        getInitialDataFromFirebase();
        convertCheckInDateCount();

        //Initialize Data
        InitializeAllViews();

        //Animation Test
//        binding.growthImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                YoYo.with(Techniques.BounceIn)
//                        .duration(1000)
//                        .playOn(binding.growthImage);
//            }
//        }
//        )
//        ;
    }

    private void setListener() {
        binding.checkInBtn.setOnClickListener(v -> clickCheckInBtn());
        binding.friendlistIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FriendList.class);
                intent.putExtra(Constants.KEY_USERNAME, username);
                intent.putExtra("userId", userId);
                intent.putExtra("shareMode", false);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        binding.myFriendsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FriendList.class);
                intent.putExtra(Constants.KEY_USERNAME, username);
                intent.putExtra("userId", userId);
                intent.putExtra("shareMode", false);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        binding.myNotesIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), NoteActivity.class));
            }
        });
        binding.myNotesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), NoteActivity.class));
            }
        });
    }

    private void getInitialDataFromFirebase() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_USERNAME, username)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            currentCheckInDateCount = Integer.parseInt(documentSnapshot.get(CHECKIN_DATE_COUNT).toString());
                            convertCheckInDateCount();
                            InitializeAllViews();
                    } else {
                        Log.e("error","there is an error");
                    }
                });
    }

    private void convertCheckInDateCount() {
        currentMileStone = currentCheckInDateCount / MILESTONE_INTERVAL;
        progress = (100 / MILESTONE_INTERVAL) * (currentCheckInDateCount % MILESTONE_INTERVAL);
    }

    private void clickCheckInBtn() {

        if (checkInValidation()) {
            //Animation
//            YoYo.with(Techniques.RubberBand)
//                    .duration(500)
//                    .repeat(1)
//                    .playOn(binding.checkInDateImage);
//            WaterAnimation();

            //Increment Current CheckIn Date Count
            currentCheckInDateCount++;
            currentMileStone = currentCheckInDateCount / MILESTONE_INTERVAL;

            //Update check in date count at firebase
            updateCheckInDateCountAtFirebase();

            //Increment ProgressBar
            incrementProgressBar();

            //Toast
            Toast.makeText(GrowthActivity.this, "CheckIn Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(GrowthActivity.this, "Wait Longer", Toast.LENGTH_SHORT).show();
        }


        InitializeAllViews();

        //Junk codes: Evaluate testing time.
        Long myLong = System.currentTimeMillis();
        double test1 = (double) myLong;
        Log.d("CurerntTime in Double", Double.toString(test1 / (TIME_INTERVAL)));
        Log.d("LastTime in Long", Long.toString(lastCheckInTime));
        Log.d("CurrentTime in Long", Long.toString(myLong / (TIME_INTERVAL)));

    }

    private void updateCheckInDateCountAtFirebase() {
        HashMap<String, Object> newData = new HashMap<>();
        newData.put(CHECKIN_DATE_COUNT, currentCheckInDateCount);
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .update(newData);
    }

    private boolean checkInValidation() {
        if (lastCheckInTime == null) {
            lastCheckInTime = System.currentTimeMillis()/ (TIME_INTERVAL);
            return true;
        }else{
            Long currentTime = System.currentTimeMillis()/ (TIME_INTERVAL);
            if (currentTime - lastCheckInTime >= 1) {
                lastCheckInTime = currentTime;
                return true;
            }else{
                return false;
            }
        }
    }

    private void incrementProgressBar() {

        int progressBarIncrementAmount = 100 / MILESTONE_INTERVAL;

        progress += progressBarIncrementAmount;

        if (progress >= 100) {
            progress = 0;
        }

        binding.progressBar.setProgress(progress);
    }

    private void InitializeAllViews() {
        //Change TextView of checkInDateText
        binding.checkInDateText.setText(String.valueOf(currentCheckInDateCount));

        //Change image based on milestones
        Log.d("CurrentMileStone", String.valueOf(currentMileStone));
        switch(currentMileStone) {
            case 0:
                binding.growthImage.setImageResource(R.drawable.prestige0);
                break;
            case 1:
                binding.growthImage.setImageResource(R.drawable.prestige1);
                break;
            case 2:
                binding.growthImage.setImageResource(R.drawable.prestige2);
                break;
            case 3:
                binding.growthImage.setImageResource(R.drawable.prestige3);
                break;
            default:
                binding.growthImage.setImageResource(R.drawable.prestige4);
                break;
        }

        //Set up progressbar textview
        String numerator = Integer.toString(currentCheckInDateCount % MILESTONE_INTERVAL);
        String dominator = Integer.toString(MILESTONE_INTERVAL);
        binding.progressText.setText(numerator + "/" + dominator);

        //Set Up Username Greeting
        binding.usernameGreeting.setText(username);
        binding.userGreetingEmail.setText(userEmail);

        //Set Up Progress Bar
        binding.progressBar.setProgress(progress);
    }

//    private void WaterAnimation() {
//        binding.kettle.setVisibility(View.VISIBLE);
//        YoYo.with(Techniques.Wave)
//                .duration(500)
//                .repeat(0)
//                .playOn(binding.kettle);
//        YoYo.with(Techniques.FadeOut)
//                .duration(500)
//                .repeat(0)
//                .playOn(binding.kettle);
//    }
}