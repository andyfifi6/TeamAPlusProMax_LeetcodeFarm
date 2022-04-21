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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


import neu.finalPro.LeetcodeFarm.databinding.ActivityNoteBinding;
import neu.finalPro.LeetcodeFarm.note.adapters.NotesAdapter;
import neu.finalPro.LeetcodeFarm.note.entities.Note;
import neu.finalPro.LeetcodeFarm.note.liseners.NotesListener;
import neu.finalPro.LeetcodeFarm.utility.Constants;
import neu.finalPro.LeetcodeFarm.utility.PreferenceManager;

public class NoteActivity extends AppCompatActivity implements NotesListener {
    private ActivityNoteBinding binding;
    private String userId;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private PreferenceManager preferenceManager;
    private int noteClickPosition = -1;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        userId = preferenceManager.getString(Constants.KEY_USER_ID);
        setListeners();
        try {
            getNotes();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
            startActivity(intent);
        });


    }

    @Override
    public void onNoteClick(Note note, int position) {
        noteClickPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("ViewNote", true);
        intent.putExtra("note", note);
        startActivity(intent);
    }

    private void getNotes() throws ParseException {
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
                            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd yyyy HH:mm a");
                            Collections.sort(noteList, new Comparator<Note>() {
                                @Override
                                public int compare(Note note, Note t1) {
                                    int res = 0;
                                    try {
                                        Date start = sdf.parse(note.getDateTime());
                                        Date end = sdf.parse(t1.getDateTime());
                                        res = end.compareTo(start);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    return res;
                                }
                            });
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
        try {
            getNotes();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}