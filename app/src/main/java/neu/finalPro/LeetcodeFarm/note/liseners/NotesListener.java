package neu.finalPro.LeetcodeFarm.note.liseners;

import neu.finalPro.LeetcodeFarm.note.entities.Note;

public interface NotesListener {
    void onNoteClick(Note note, int position);
}
