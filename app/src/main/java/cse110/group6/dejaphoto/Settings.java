package cse110.group6.dejaphoto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Settings extends AppCompatActivity implements OnItemSelectedListener {
    public static final int REQUEST_CODE = 420;
    Uri imgUri;
    Spinner spinnerDialog;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("Settings");


        adapter = ArrayAdapter.createFromResource(this, R.array.time_entries, android.R.layout.simple_spinner_item);

        spinnerDialog = (Spinner) findViewById(R.id.spinner_dialog);
        spinnerDialog.setAdapter(adapter);
        spinnerDialog.setOnItemSelectedListener(Settings.this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TextView spinnerDialogText = (TextView) view;
        Toast.makeText(this, "Background changes every " + spinnerDialogText.getText(), Toast.LENGTH_SHORT).show();

        String size = spinnerDialog.getSelectedItem().toString();
        int spinner_pos = spinnerDialog.getSelectedItemPosition();
        String[] size_values = getResources().getStringArray(R.array.time_values);
        long actual_size = Long.valueOf(size_values[spinner_pos]);

        Intent timeChange = new Intent();
        timeChange.putExtra("newtime", actual_size);
        setResult(RESULT_OK, timeChange);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUri = data.getData();

            try {
                Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                Toast.makeText(getApplicationContext(), "Image has been set",Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void choosePhotos_btn(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_CODE);
    }


}
