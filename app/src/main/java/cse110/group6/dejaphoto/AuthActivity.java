package cse110.group6.dejaphoto;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Created by RaiJin on 5/30/2017. <- thanks RaiJin for the copy pasta!!!
 */

public class AuthActivity extends AppCompatActivity{

    private String TAG = "AuthActivity";

    //UI References
    private EditText mEmail, mPassword;
    private Button btnSignIn, btnSignOut, btnLaunchApp;
    private Boolean valid_user = false;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.auth_main);

        //declare buttons and edit texts in oncreate
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        btnSignIn = (Button) findViewById(R.id.email_sign_in_button);
        btnSignOut = (Button) findViewById(R.id.email_sign_out_button);
        btnLaunchApp = (Button) findViewById(R.id.launch_app);

        mAuth = FirebaseAuth.getInstance();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    // Set display name
                    UserProfileChangeRequest.Builder displayName = new UserProfileChangeRequest.Builder();
                    String accountName = user.getEmail();
                    displayName.setDisplayName(accountName.substring(0, accountName.lastIndexOf("@")));
                    user.updateProfile(displayName.build());

                    toastMessages("Successfully signed in with: " + user.getEmail());
                    valid_user = true;
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessages("Successfully signed out");
                }
                // ...
            }
        };

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                String pass = mPassword.getText().toString();
                if (!email.equals("") && !pass.equals("")) {
                    mAuth.signInWithEmailAndPassword(email, pass);
                } else {
                    Toast.makeText(getApplicationContext(), "No fields", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(valid_user == true) {
                    mAuth.signOut();
                    valid_user = false;
                    toastMessages("Signing Out...");
                }
                else {
                    toastMessages("Already signed out");
                }
            }
        });

        btnLaunchApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (valid_user == true){
                    Log.d(TAG, "onClick: Switching Activities.");
                    //  toastMessages("Not yet implemented");
                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else
                    toastMessages("Invalid user, please sign in");

            }
        });


    } //end of onCreate

    @Override
    public void onStart () {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop () {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //ToastMessage Function

    private void toastMessages(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
} //end of AuthActivity
