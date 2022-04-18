package neu.finalPro.LeetcodeFarm.note;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import neu.finalPro.LeetcodeFarm.R;
import neu.finalPro.LeetcodeFarm.databinding.ActivityCreateNoteBinding;
import neu.finalPro.LeetcodeFarm.databinding.ActivityNoteBinding;
import neu.finalPro.LeetcodeFarm.note.database.NoteDatabase;
import neu.finalPro.LeetcodeFarm.note.entities.Note;

public class CreateNoteActivity extends AppCompatActivity {

    private ActivityCreateNoteBinding binding;

    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDateTime;
    private ImageView image1, image2, image3;
    private ImageView removeImage1;

    private String imagePath;
    private Note availableNote;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        imagePath = "";

        if (getIntent().getBooleanExtra("ViewNote", false)) {
            availableNote = (Note) getIntent().getSerializableExtra("note");
            setViewNote();
        }

        inputNoteTitle = binding.inputNoteTitle;
        inputNoteSubtitle = binding.inputNoteSubtitle;
        inputNoteText = binding.inputNote;
        textDateTime = binding.textDateTime;
        image1 = binding.image1;
        removeImage1 = binding.removeImage1;

        removeImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image1.setImageBitmap(null);
                image1.setVisibility(View.GONE);
                removeImage1.setVisibility(View.GONE);
                imagePath = "";
            }
        });

        binding.textDateTime.setText(
                new SimpleDateFormat("EEEE, MMMM dd yyyy HH:mm a", Locale.getDefault()).format(new Date())
        );

    }

    private void setViewNote() {
        binding.inputNoteTitle.setText(availableNote.getTitle());
        binding.inputNoteSubtitle.setText(availableNote.getSubtitle());
        binding.inputNote.setText(availableNote.getNoteText());
        binding.textDateTime.setText(availableNote.getDateTime());

        if (availableNote.getImagePath() != null && !availableNote.getImagePath().trim().isEmpty()) {
            binding.image1.setImageBitmap(BitmapFactory.decodeFile(availableNote.getImagePath()));
            binding.image1.setVisibility(View.VISIBLE);
            binding.removeImage1.setVisibility(View.VISIBLE);
            imagePath = availableNote.getImagePath();
        }

    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
        binding.addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            }
        });

    }


    private void saveNote(){
        if(inputNoteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Enter a title", Toast.LENGTH_LONG).show();
            return;
        } else if(inputNoteSubtitle.getText().toString().trim().isEmpty() && inputNoteText.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Enter a anything", Toast.LENGTH_LONG).show();
            return;
    }
        final Note note = new Note();
        // TODO: store info to note in database.
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setImagePath(imagePath);

        if (availableNote != null) {
            note.setId(note.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void>{

            @Override
            protected Void doInBackground(Void... voids){
                NoteDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid){
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        image1.setImageBitmap(bitmap);
                        image1.setVisibility(View.VISIBLE);
                        removeImage1.setVisibility(View.VISIBLE);
                        //save image path
                        imagePath = getPathFromUri(selectedImageUri);

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        String filePath;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            filePath = uri.getPath();
        } else  {
            cursor.moveToFirst();
            int i = cursor.getColumnIndex("_data");
            filePath = cursor.getString(i);
            cursor.close();
        }
        return filePath;
    }
    
}