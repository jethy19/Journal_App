package com.example.self;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;


public class CreateAccountActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser curentuser;
    //Firebase connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");
    private EditText emailEdittext;
    private EditText passwordEdittext;
    private Button createAccButton;
    private ProgressBar progressBar;
    private EditText usernameedittext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth = FirebaseAuth.getInstance();

        createAccButton = findViewById(R.id.email_signup_button);
        progressBar = findViewById(R.id.createacctprogress);
        emailEdittext = findViewById(R.id.email_account);
        passwordEdittext = findViewById(R.id.password_account);
        usernameedittext = findViewById(R.id.username_account);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                curentuser = firebaseAuth.getCurrentUser();
                if(curentuser != null)
                { //user logged in

                }
                else
                {

                }
            }
        };

        createAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(emailEdittext.getText().toString()) &&
                        !TextUtils.isEmpty(passwordEdittext.getText().toString()) && !TextUtils.isEmpty(usernameedittext.getText().toString())) {
                    String email = emailEdittext.getText().toString().trim();
                    String password = passwordEdittext.getText().toString().trim();
                    String username = usernameedittext.getText().toString().trim();
                    createUserEmailAccount(email, password, username);
                }
                else
                {
                    Toast.makeText(CreateAccountActivity.this, "Empty Fiels Not Allowed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void createUserEmailAccount(String email,String password, String username)
    {
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username))
        {
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        curentuser = firebaseAuth.getCurrentUser();
                        String currentUserId = curentuser.getUid();

                        Map<String,String> userobj = new HashMap<>();
                        userobj.put("userID",currentUserId);
                        userobj.put("username",username);

                        //save the user
                        collectionReference.add(userobj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(Objects.requireNonNull(task.getResult().exists()))
                                        {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            String name = task.getResult().getString("username");
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUserID(currentUserId);
                                            journalApi.setUsername(name);

                                            Intent intent  = new Intent(CreateAccountActivity.this,PostJournalActivity.class);
                                            intent.putExtra("username",name);
                                            intent.putExtra("userId",currentUserId);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else {
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                    else
                    {

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
        else
        {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        curentuser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}