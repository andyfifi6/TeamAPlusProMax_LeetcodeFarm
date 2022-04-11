package neu.finalPro.LeetcodeFarm.note.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import neu.finalPro.LeetcodeFarm.databinding.ItemContainerNoteBinding;
import neu.finalPro.LeetcodeFarm.note.entities.Note;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;

    public NotesAdapter(List<Note> notes) {
        this.notes = notes;
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
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
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
}
