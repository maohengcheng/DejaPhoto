package cse110.group6.dejaphoto;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by RaiJin on 5/30/2017.
 */

public class AuthActivity extends AppCompatActivity{
    private String TAG = "AuthActivity";
    private Button btnSignIn, btnSignOut, btnUploadImage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.auth_main);

        btnUploadImage = (Button) findViewById(R.id.upload_image);
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Switching Activities.");
              //  toastMessages("Not yet implemented");
                Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    //ToastMessage Function

    private void toastMessages(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
} //end of AuthActivity
