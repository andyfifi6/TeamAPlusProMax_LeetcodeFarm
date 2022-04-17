package neu.finalPro.LeetcodeFarm.note.adapters;

import static android.os.Looper.*;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogRecord;

import neu.finalPro.LeetcodeFarm.databinding.ItemContainerNoteBinding;
import neu.finalPro.LeetcodeFarm.note.entities.Note;
import neu.finalPro.LeetcodeFarm.note.liseners.NotesListener;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> noteSource;

    public NotesAdapter(List<Note> notes, NotesListener notesListener) {

        this.notes = notes;
        this.notesListener = notesListener;
        noteSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerNoteBinding itemContainerNoteBinding = ItemContainerNoteBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new NoteViewHolder(itemContainerNoteBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setNote(notes.get(position));
        holder.binding.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListener.onNoteClick(notes.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

        ItemContainerNoteBinding binding;
        LinearLayout layoutNote;

        NoteViewHolder(ItemContainerNoteBinding itemContainerNoteBinding) {
            super(itemContainerNoteBinding.getRoot());
            binding = itemContainerNoteBinding;
        }

        void setNote(Note note){
            binding.textTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()){
                binding.textSubtitle.setVisibility(View.GONE);
            } else {
                binding.textSubtitle.setText(note.getSubtitle());
            }
            binding.textDateTime.setText(note.getDateTime());
        }

    }

    public void searchNotes(final String keyword) {
        String l_keyword = keyword.toLowerCase();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (keyword.trim().isEmpty()) {
                    notes = noteSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note n : noteSource) {
                        if (n.getTitle().toLowerCase().contains(l_keyword)
                            || n.getSubtitle().toLowerCase().contains(l_keyword)) {
                            temp.add(n);
                        }
                    }
                    notes = temp;
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
