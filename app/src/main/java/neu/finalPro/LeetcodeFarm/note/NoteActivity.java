package neu.finalPro.LeetcodeFarm.note;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


import neu.finalPro.LeetcodeFarm.R;
import neu.finalPro.LeetcodeFarm.databinding.ActivityNoteBinding;
import neu.finalPro.LeetcodeFarm.note.adapters.NotesAdapter;
import neu.finalPro.LeetcodeFarm.note.entities.Note;
import neu.finalPro.LeetcodeFarm.note.utils.MyButtonClickListener;
import neu.finalPro.LeetcodeFarm.note.utils.NotesListener;
import neu.finalPro.LeetcodeFarm.note.utils.SwipeHelper;
import neu.finalPro.LeetcodeFarm.user.MainActivity;
import neu.finalPro.LeetcodeFarm.utility.Constants;
import neu.finalPro.LeetcodeFarm.utility.PreferenceManager;

public class NoteActivity extends AppCompatActivity implements NotesListener {
    private ActivityNoteBinding binding;
    private String userId;
    private List<Note> noteList = new ArrayList<>();
    private NotesAdapter notesAdapter;
    private PreferenceManager preferenceManager;
    private int noteClickPosition = -1;
    private AlertDialog deleteNoteDialog = null;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        userId = preferenceManager.getString(Constants.KEY_USER_ID);
        setListeners();
        init();
    }

    private void init(){
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        binding.notesRecyclerView.setAdapter(notesAdapter);
        binding.notesRecyclerView.setVisibility(View.VISIBLE);
        try {
            getNotes();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SwipeHelper swipeHelper = new SwipeHelper(this, binding.notesRecyclerView, 150) {
            @Override
            protected void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<SwipeHelper.MyButton> buffer) {
                buffer.add(new MyButton("Delete", R.drawable.ic_delete_forever, 30, R.color.lightRed,
                        new MyButtonClickListener() {
                            @Override
                            public void onClick(int position) {
                                Log.d("position in swipe", String.valueOf(position));
                                showDeleteNoteDialog(position);
                            }
                        }, NoteActivity.this));
            }
        };
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
        binding.imageBack.setOnClickListener(v -> {
            Intent mainPage = new Intent(getApplicationContext(), MainActivity.class);
            mainPage.putExtra("userId", userId);
            startActivity(mainPage);
        });

        binding.addNoteMain.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
            intent.putExtra("ViewNote", false);
            startActivity(intent);
        });


    }

    private void showDeleteNoteDialog(int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
        View v = LayoutInflater.from(this).inflate(
                R.layout.layout_delete_note, (ViewGroup) findViewById(R.id.layoutDeleteNote)
        );
        builder.setView(v);
        deleteNoteDialog = builder.create();
        if (deleteNoteDialog.getWindow() != null) {
            deleteNoteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        v.findViewById(R.id.textNoteDelete).setOnClickListener(new View.OnClickListener() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onClick(View view) {
            deleteNote(pos);
            noteList.remove(pos);
            notesAdapter.notifyDataSetChanged();
            deleteNoteDialog.dismiss();
        }
        });

        v.findViewById(R.id.textNoteCancelDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNoteDialog.dismiss();
            }
        });
        deleteNoteDialog.show();
    }

    private void deleteNote(int pos) {
        Note noteToDelete = noteList.get(pos);
        Log.d("note to delete", noteToDelete.toString());
        database.collection("notes").document(noteToDelete.getId())
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NoteActivity.this, "Note deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NoteActivity.this, "Fail to delete this note", Toast.LENGTH_SHORT).show();
                }
            }
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
                            note.setWebLink(queryDocumentSnapshot.getString("webLink"));
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
                            notesAdapter = new NotesAdapter(noteList, this);
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