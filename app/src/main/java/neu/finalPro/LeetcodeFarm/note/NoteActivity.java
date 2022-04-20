package neu.finalPro.LeetcodeFarm.note;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;


import neu.finalPro.LeetcodeFarm.databinding.ActivityNoteBinding;
import neu.finalPro.LeetcodeFarm.note.adapters.NotesAdapter;
import neu.finalPro.LeetcodeFarm.note.entities.Note;
import neu.finalPro.LeetcodeFarm.note.liseners.NotesListener;

public class NoteActivity extends AppCompatActivity implements NotesListener {
    private ActivityNoteBinding binding;
    private String userId;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private int noteClickPosition = -1;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userId = getIntent().getStringExtra("userId");
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
        binding.addNoteMain.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
            intent.putExtra("ViewNote", false);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });


    }

    @Override
    public void onNoteClick(Note note, int position) {
        noteClickPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("ViewNote", true);
        intent.putExtra("note", note);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void getNotes() {
        database.collection("notes")
                .whereEqualTo("userId",userId)
                .get()
                .addOnCompleteListener(task -> {
                    noteList.clear();
                    if (task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                            Note note = new Note();
                            note.setTitle(queryDocumentSnapshot.getString("title"));
                            note.setSubtitle(queryDocumentSnapshot.getString("subtitle"));
                            note.setNoteText(queryDocumentSnapshot.getString("noteText"));
                            note.setDateTime(queryDocumentSnapshot.getString("dateTime"));
                            note.setImagePath(queryDocumentSnapshot.getString("imagePath"));
                            note.setId(queryDocumentSnapshot.getId());
                            note.setUserId(userId);
                            noteList.add(note);
                        }
                        if( noteList.size() > 0 ){
                            NotesAdapter notesAdapter = new NotesAdapter(noteList, this);
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
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //Refresh your stuff here
        getNotes();
    }

}