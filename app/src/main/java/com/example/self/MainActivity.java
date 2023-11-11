package com.example.self;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.JournalApi;
public class MainActivity extends AppCompatActivity {

    private Button getsartedButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentuser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getsartedButton = findViewById(R.id.startButton);
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentuser = firebaseAuth.getCurrentUser();
                if(currentuser != null)
                {
                    currentuser = firebaseAuth.getCurrentUser();
                    final String currentUserId = currentuser.getUid();
                    collectionReference.whereEqualTo("userID",currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if(error != null)
                                    {
                                        return;
                                    }
                                    String name;
                                    if(!value.isEmpty())
                                    {
                                        for(QueryDocumentSnapshot snapshot : value)
                                        {
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUserID(snapshot.getString("userID"));
                                            journalApi.setUsername(snapshot.getString("username"));

                                            startActivity(new Intent(MainActivity.this,JournalListActivity.class));
                                            finish();
                                        }
                                    }
                                }
                            });
                }
                else
                {

                }
            }
        };

        getsartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        currentuser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth != null)
        {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}