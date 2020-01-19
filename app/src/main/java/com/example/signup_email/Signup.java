package com.example.signup_email;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {
    private EditText inputEmail, inputPassword, inputPhone, inputName;
    private  String name, phone, email, password, user_id;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference dbrf;
    private FirebaseDatabase firebaseDatabase;

   // public static final String TAG = " ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
       // dbrf  = firebaseDatabase.getReference();

       // databaseReference = FirebaseDatabase.getInstance().getReference();

        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputName = (EditText) findViewById(R.id.name_signup);
        inputEmail = (EditText) findViewById(R.id.email_signup);
        inputPhone = (EditText) findViewById(R.id.phone_signup);
        inputPassword = (EditText) findViewById(R.id.password_signup);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);


        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Signup.this, MainActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 name = inputName.getText().toString().trim();
                 phone = inputPhone.getText().toString().trim();
                 email = inputEmail.getText().toString().trim();
                 password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getApplicationContext(), "Enter Name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(getApplicationContext(), "Enter Phone!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                 auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(Signup.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(Signup.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    checkAccountEmailExistInFirebase(email);
                                    // This is for storing authenticated user's data
                                    dbrf = FirebaseDatabase.getInstance().getReferenceFromUrl("https://signupemail-50215.firebaseio.com/").child("users");
                                    FirebaseUser firebaseUser = auth.getCurrentUser();
                                    user_id = firebaseUser.getUid();  // we get Uid
                                    user_id  = dbrf.push().getKey();
                                    sendVerificationEmail();
                                    writeNewUser(user_id, name, email, phone, password);
                                    Toast.makeText(Signup.this, "Successfully Registered", Toast.LENGTH_LONG).show();
                                    user_signIn();
                                    Intent i = new Intent(Signup.this, ResetPassword.class);
                                    startActivity(i);
                                    finish();

                                }
                            }
                        });

            }
        }
        );

    }

    private void user_signIn() {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //Log.d("TAG", "signInWithEmail:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Log.w("TAG", "signInWithEmail:failed", task.getException());

                } else {
                    checkIfEmailVerified();
                }
                // ...
            }
        });
            }

    private void checkIfEmailVerified() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.isEmailVerified())
        {
            // User is verified so we can finish this activity
            Toast.makeText(Signup.this, "Email is Verified and Successfully Logged in !!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Signup.this, ResetPassword.class);
            startActivity(intent);
            finish();
        }
        else {
            // Email is not verified so we logout the user and restart the activity
            FirebaseAuth.getInstance().signOut();
            // Restart Activity
            finish();
            startActivity(getIntent());
        }
    }



    private void sendVerificationEmail() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    // Email Sent
                    // After email is sent just logout the user and finish this activity
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(Signup.this, ResetPassword.class));
                    finish();
                }
                else {
                    // Email not sent, restart the activity
                    overridePendingTransition(0, 0);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());

                }
            }
        });
    }

    private boolean checkAccountEmailExistInFirebase(String email) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final boolean[] b = new boolean[1];
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                b[0] = !task.getResult().getSignInMethods().isEmpty();
            }
        });
        return b[0];
    }

//    OnCompleteListener<AuthResult> completeListener = new OnCompleteListener<AuthResult>() {
//        @Override
//        public void onComplete(@NonNull Task<AuthResult> task) {
//            if (task.isSuccessful()) {
//                boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
//                if (isNewUser) {
//
//                } else {
//                    Log.d("TAG", "Is Old User!");
//                }
//            }
//        }
//    };


    private void writeNewUser(String user_id1, final String name1, final String email1, final String phone1, final String password1) {
//        User user = new User(name, email, phone, password);
//        databaseReference.child(user_id).setValue(user);

        final DatabaseReference usersRef = dbrf.child("users").child(user_id1);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                    usersRef.setValue(new User(name1, email1, phone1, password1));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
//    public void isCheckEmail()
//    {
//        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
//            @Override
//            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
//                Log.d(TAG,""+task.getResult().getSignInMethods().size());
//                if (task.getResult().getSignInMethods().size() == 0){
//
//                    // email not existed
//
//                }else {
//                    // email existed
//                    Toast.makeText(Signup.this, "Email already exists !!", Toast.LENGTH_SHORT).show();
//            }}
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                e.printStackTrace();
//            }
//        });
  //  }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}


