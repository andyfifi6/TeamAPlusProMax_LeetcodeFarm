package neu.finalPro.LeetcodeFarm.note.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;


import neu.finalPro.LeetcodeFarm.note.entities.Note;


@Dao
public interface NoteDao {

//    @Query("SELECT * FROM notes ORDER BY id")
//    List<Note> getAllNotes();
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insertNote(Note note);
//
//    @Delete
//    void deleteNote(Note note);
}
