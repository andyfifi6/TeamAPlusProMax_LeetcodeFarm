package neu.finalPro.LeetcodeFarm.note;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;


import neu.finalPro.LeetcodeFarm.R;
import neu.finalPro.LeetcodeFarm.databinding.ActivityNoteBinding;
import neu.finalPro.LeetcodeFarm.note.adapters.NotesAdapter;
import neu.finalPro.LeetcodeFarm.note.database.NoteDatabase;
import neu.finalPro.LeetcodeFarm.note.entities.Note;
import neu.finalPro.LeetcodeFarm.note.utils.MyButtonClickListener;
import neu.finalPro.LeetcodeFarm.note.utils.NotesListener;
import neu.finalPro.LeetcodeFarm.note.utils.SwipeHelper;

public class NoteActivity extends AppCompatActivity implements NotesListener {

    public static final String REQUEST_CODE = "requestCode";
    public static final int REQUEST_ADD_NOTE = 1;
    public static final int REQUEST_UPDATE_NOTE = 2;
    public static final int REQUEST_SHOW_NOTES = 3;

    private ActivityNoteBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private AlertDialog deleteNoteDialog = null;

    private int noteClickPosition = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        getNotes(REQUEST_SHOW_NOTES);
        init();
    }

    private void init(){
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        binding.notesRecyclerView.setAdapter(notesAdapter);
        binding.notesRecyclerView.setVisibility(View.VISIBLE);
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

    private void showDeleteNoteDialog(int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
        View v = LayoutInflater.from(this).inflate(
                R.layout.layout_delete_note, (ViewGroup) findViewById(R.id.layoutDeleteNote)
        );
        builder.setView(v);
        deleteNoteDialog = builder.create();
        v.findViewById(R.id.textNoteDelete).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                //NoteDatabase.getDatabase(getApplicationContext())
                //        .noteDao().deleteNote(noteList.get(position));
                noteList.remove(pos);
                notesAdapter.notifyDataSetChanged();
                Toast.makeText(NoteActivity.this, "Note Deleted Successfully", Toast.LENGTH_SHORT).show();
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

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());

        /*activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new myActivityResult());

        binding.addNoteMain.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), CreateNoteActivity.class);
            i.putExtra(REQUEST_CODE, REQUEST_ADD_NOTE);
            activityResultLauncher.launch(i);
        });*/


        binding.addNoteMain.setOnClickListener(v -> {
            startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class),
                    REQUEST_ADD_NOTE);

        });

    }

    @Override
    public void onNoteClick(Note note, int position) {
        noteClickPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("ViewNote", true);
        intent.putExtra("note", note);
        intent.putExtra(REQUEST_CODE, REQUEST_UPDATE_NOTE);
        startActivityForResult(intent, REQUEST_UPDATE_NOTE);
    }

    private void getNotes(int requestCode) {

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes){
                super.onPostExecute(notes);
                if (requestCode == REQUEST_SHOW_NOTES) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_ADD_NOTE) {
                    Log.d("add note", "add new");
                    noteList.add(0, notes.get(notes.size()-1));
                    notesAdapter.notifyItemInserted(0);
                    binding.notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_UPDATE_NOTE) {
                    Log.d("update note", String.valueOf(noteClickPosition));
                    noteList.remove(noteClickPosition);
                    noteList.add(noteClickPosition, notes.get(noteClickPosition));
                    notesAdapter.notifyItemChanged(noteClickPosition);
                }
            }
        }

        new GetNotesTask().execute();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_NOTE && resultCode  ==RESULT_OK) {
            getNotes(REQUEST_ADD_NOTE);
        } else if (requestCode == REQUEST_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_UPDATE_NOTE);
            }
        }
    }



 /*   private class myActivityResult implements ActivityResultCallback<ActivityResult> {

        @Override
        public void onActivityResult(ActivityResult result) {
            Log.d("resultCode=", String.valueOf(result.getResultCode()));
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Log.d("data", data.toString());
                if (data != null) {
                    int code = data.getIntExtra(REQUEST_CODE, 0);
                    Log.d("requestCode=", String.valueOf(code));
                    if (code == REQUEST_ADD_NOTE) {
                        getNotes(REQUEST_ADD_NOTE);
                    } else if (code == REQUEST_UPDATE_NOTE) {
                        if (data.getBooleanExtra("ViewNote", false)) {
                            getNotes(REQUEST_UPDATE_NOTE);
                        }
                    }
                }
            }
        }
    }*/
}