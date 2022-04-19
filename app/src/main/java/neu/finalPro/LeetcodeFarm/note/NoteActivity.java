package neu.finalPro.LeetcodeFarm.note;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.google.firebase.database.Query;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import neu.finalPro.LeetcodeFarm.databinding.ActivityNoteBinding;
import neu.finalPro.LeetcodeFarm.note.adapters.NotesAdapter;
import neu.finalPro.LeetcodeFarm.note.entities.Note;
import neu.finalPro.LeetcodeFarm.note.liseners.NotesListener;

public class NoteActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_SHOW_NOTES = 3;

    private ActivityNoteBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private int noteClickPosition = -1;

    //database
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private Query query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        getNotes();
        init();
    }

    private void init(){
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        binding.notesRecyclerView.setAdapter(notesAdapter);
        binding.notesRecyclerView.setVisibility(View.VISIBLE);
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (noteList.size() != 0) {
                    notesAdapter.searchNotes(editable.toString());
                }
            }
        });

    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK){
                            getNotes();
                        }
                    }
                });
        binding.addNoteMain.setOnClickListener(v -> {
            activityResultLauncher.launch(
                    new Intent(getApplicationContext(), CreateNoteActivity.class));

        });

    }

    @Override
    public void onNoteClick(Note note, int position) {
        noteClickPosition = position;
        String userId = getUserId();
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("ViewNote", true);
        intent.putExtra("note", note);
        startActivity(intent);
    }

    private void getNotes() {
        String currentUserId = getUserId();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Log.d("run_Back", "success");
                database.collection("Notes")
                        .whereEqualTo("userId",currentUserId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null){
                                List<Note> notes = new ArrayList<>();
                                for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                                    if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                        continue;
                                    }
                                    Note note = new Note();
                                    note.title = queryDocumentSnapshot.getString("title");
                                    note.subtitle= queryDocumentSnapshot.getString("subtitle");
                                    note.noteText= queryDocumentSnapshot.getString("noteText");
                                    note.dateTime= queryDocumentSnapshot.getString("dateTime");
                                    note.imagePath= queryDocumentSnapshot.getString("imagePath");
                                    note.id = queryDocumentSnapshot.getId();
                                    notes.add(note);
                                }
                                if(notes.size() > 0 ){
                                    NotesAdapter notesAdapter = new NotesAdapter(notes, this);
                                    binding.notesRecyclerView.setAdapter(notesAdapter);
                                    binding.notesRecyclerView.setVisibility(View.VISIBLE);
                                } else {
                                    binding.notesRecyclerView.setVisibility(View.VISIBLE);
                                    Log.e("No_notes", "Query notes error, note size is 0.");
                                }
                            } else {
                                Log.e("No_notes", "Query notes error");
                            }
                        });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("run_finished", "success");
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }

        });

       /* @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes){
                super.onPostExecute(notes);
                if (noteList.size() == 0) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else {
                    noteList.add(0, notes.get(notes.size()-1));
                    notesAdapter.notifyItemInserted(0);
                }
                binding.notesRecyclerView.smoothScrollToPosition(0);
            }
        }

        new GetNotesTask().execute(); */
    }
    private String getUserId(){
        Intent intent = getIntent();
        return intent.getStringExtra("id");
    }
}