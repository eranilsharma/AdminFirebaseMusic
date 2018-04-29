package android.sharma.com.onlinemusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AdminActivity extends AppCompatActivity {

    private static final int REQUEST_BROWSE = 123;
    private static final String TAG = "";
    Uri musicuri;
    EditText edtMusicName,edtArtistName;
    AutoCompleteTextView edtMusicCategory;
    RelativeLayout relativeLayout;
    ProgressBar progressBar;
    private StorageReference storageReference = null;
    private DatabaseReference mRef;
    Button btnChooseMusic;
    String[] categorylist = {"Hindi", "Punjabi", "English", "Pahari "};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        btnChooseMusic = (Button) findViewById(R.id.btnchoosemusic);
        edtMusicName = (EditText) findViewById(R.id.edtmusicname);
        edtArtistName = (EditText) findViewById(R.id.edtArtistname);
        edtMusicCategory = (AutoCompleteTextView) findViewById(R.id.edtmusiccategory);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativelayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        storageReference = FirebaseStorage.getInstance().getReference();
        mRef = FirebaseDatabase.getInstance().getReference("Songs");

        ArrayAdapter adapter = new
                ArrayAdapter(this, android.R.layout.simple_list_item_1, categorylist);

        edtMusicCategory.setAdapter(adapter);
        edtMusicCategory.setThreshold(1);
    }

    public void btnchoosemusicclicked(View view) {
        Intent intent = new Intent();
        intent.setType("audio/mpeg");
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent = Intent.createChooser(intent, "Select Music");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            String[] mimetypes = {"audio/mpeg"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        }
        startActivityForResult(intent, REQUEST_BROWSE);
    }

    public void btnuploadmusicclicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        relativeLayout.setVisibility(View.GONE);
        final String musicname = edtMusicName.getText().toString().trim();
        final String artistname = edtArtistName.getText().toString().trim();
        final String musiccategory = edtMusicCategory.getText().toString().trim();
        if (!TextUtils.isEmpty(musicname) &&!TextUtils.isEmpty(artistname)&& !TextUtils.isEmpty(musiccategory)) {
            final StorageReference newmusic = storageReference.child(musicuri.getLastPathSegment());
            newmusic.putFile(musicuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadmusicuri = taskSnapshot.getDownloadUrl();
                    Toast.makeText(AdminActivity.this, "Music Upload Successfull", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    relativeLayout.setVisibility(View.VISIBLE);
                    final DatabaseReference newpost = mRef.push();
                    int like=0;
                    newpost.child("MusicName").setValue(musicname);
                    newpost.child("SongCategory").setValue(musiccategory);
                    newpost.child("artistName").setValue(artistname);
                    newpost.child("songUID").setValue(newpost.getKey().toString().trim());
                    newpost.child("Songurl").setValue(downloadmusicuri.toString());
                    newpost.child("Likes").setValue(like);
                    Toast.makeText(AdminActivity.this, "Song Uploaded Succesfull", Toast.LENGTH_LONG).show();
                    edtMusicCategory.setText("");
                    edtMusicName.setText("");
                    edtArtistName.setText("");
                    btnChooseMusic.setText("Choose Music");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    relativeLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(AdminActivity.this, "something went wrong", Toast.LENGTH_LONG).show();
                    // progressRelativeLayout.showContent();
                    Log.e("error", String.valueOf(e));

                }
            });
        } else {
            Toast.makeText(AdminActivity.this, "Please Fill all Fileds", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BROWSE && resultCode == Activity.RESULT_OK && data != null) {
            musicuri = data.getData();
            if (musicuri != null) {
                // TODO: handle your case
                btnChooseMusic.setText("Music Selected");
                Log.e(TAG, "onActivityResult: " + musicuri);
                //musicuri.parse("content://media/external/audio/albumart");
            }
        }
    }
}
