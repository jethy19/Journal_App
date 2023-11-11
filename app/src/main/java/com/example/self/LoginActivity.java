package com.example.self;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button createAccButton;
    private FirebaseAuth firebaseAuth;
    //private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentuser;
    private AutoCompleteTextView emailAddress;
    private EditText password;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //progressBar.findViewById(R.id.loginprogress);
        firebaseAuth = FirebaseAuth.getInstance();
        loginButton = findViewById(R.id.email_signin_button);
        createAccButton = findViewById(R.id.email_signup_button_login);
        emailAddress = findViewById(R.id.email);
        password = findViewById(R.id.password);


        createAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginEmailpassworduser(emailAddress.getText().toString().trim(),password.getText().toString().trim());

            }
        });
    }

    private void loginEmailpassworduser(String email, String pwd) {
        //progressBar.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd))
        {
            firebaseAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    assert user != null;
                    final String currentUserId = user.getUid();

                    collectionReference.whereEqualTo("userID",currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                    if(error != null)
                                    {

                                    }
                                    assert value != null;
                                    if(!value.isEmpty())
                                    {
                                        //progressBar.setVisibility(View.INVISIBLE);
                                        for(QueryDocumentSnapshot snapshot : value)
                                        {
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUsername(snapshot.getString("username"));
                                            journalApi.setUserID(snapshot.getString("userID"));

                                            startActivity(new Intent(LoginActivity.this,JournalListActivity.class));
                                            finish();
                                        }
                                    }
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Invalid Mail and Password", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            //progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this, "Plz Enter Email and Password", Toast.LENGTH_SHORT).show();
        }
    }
}