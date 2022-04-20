package neu.finalPro.LeetcodeFarm.note.utils;

import neu.finalPro.LeetcodeFarm.note.entities.Note;

public interface NotesListener {
    void onNoteClick(Note note, int position);
}
