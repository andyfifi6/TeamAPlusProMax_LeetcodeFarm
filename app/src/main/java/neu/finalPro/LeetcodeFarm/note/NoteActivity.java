package neu.finalPro.LeetcodeFarm.note;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;


import java.util.ArrayList;
import java.util.List;


import neu.finalPro.LeetcodeFarm.databinding.ActivityNoteBinding;
import neu.finalPro.LeetcodeFarm.note.adapters.NotesAdapter;
import neu.finalPro.LeetcodeFarm.note.database.NoteDatabase;
import neu.finalPro.LeetcodeFarm.note.entities.Note;
import neu.finalPro.LeetcodeFarm.note.liseners.NotesListener;

public class NoteActivity extends AppCompatActivity implements NotesListener {
    private ActivityNoteBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private int noteClickPosition = -1;


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
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("ViewNote", true);
        intent.putExtra("note", note);
        startActivity(intent);
    }

    private void getNotes() {

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Note> notes){
                super.onPostExecute(notes);
                Log.d("Note_List", "Size is " + noteList.size());
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

        new GetNotesTask().execute();
    }

}