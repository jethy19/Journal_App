package com.example.self;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE = 1;
    private Button saveButton;
    //private ProgressBar progressBar;
    private ImageView addPhoneButton;
    private EditText thoughtsedittext;
    private EditText titleedittext;
    private TextView currentuser;
    private ImageView imageView;

    private String curentUserID;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("Journal");
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        //progressBar = findViewById(R.id.post_progressBar);
        titleedittext = findViewById(R.id.post_title_edittext);
        thoughtsedittext = findViewById(R.id.post_thoughts_edittext);
        currentuser = findViewById(R.id.post_username_textview);

        imageView = findViewById(R.id.post_imageView);
        saveButton = findViewById(R.id.post_save_button);
        saveButton.setOnClickListener(this);
        addPhoneButton = findViewById(R.id.post_camerabutton);
        addPhoneButton.setOnClickListener(this);
        //progressBar.setVisibility(View.INVISIBLE);

        if(JournalApi.getInstance() != null)
        {
            currentUserName = JournalApi.getInstance().getUsername();
            curentUserID = JournalApi.getInstance().getUserID();

            currentuser.setText(currentUserName);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null)
                {

                }
                else
                {

                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.post_save_button)
        {
            saveJournal();
        }
        else if(view.getId() == R.id.post_camerabutton)
        {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,GALLERY_CODE);
        }
    }

    private void saveJournal() {
        String title = titleedittext.getText().toString().trim();
        String thoughts = thoughtsedittext.getText().toString().trim();
        //progressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && imageUri != null)
        {
            StorageReference filepath = storageReference.child("journal_images")
                    .child("my_image_"+ Timestamp.now().getSeconds());

            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String imageUrl = uri.toString();
                            Journal journal = new Journal();
                            journal.setTitle(title);
                            journal.setThought(thoughts);
                            journal.setImageurl(imageUrl);
                            journal.setTimeAdded(new Timestamp(new Date()));
                            journal.setUserName(currentUserName);
                            journal.setUserId(curentUserID);

                            collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    //progressBar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(PostJournalActivity.this,JournalListActivity.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });


                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
        else
        {
            //progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK)
        {
            if(data != null)
            {
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null)
        {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}