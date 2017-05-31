package cse110.group6.dejaphoto;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static cse110.group6.dejaphoto.R.layout.activity_settings;

public class Settings extends AppCompatActivity implements OnItemSelectedListener {
    Spinner spinnerDialog;
    ArrayAdapter adapter;

    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 421;
    int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;
    int MY_PERMISSION_ACCESS_COURSE_LOCATION = 5042;
    int MY_PERMISSION_ACCESS_FINE_LOCATION = 5048;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 20;
    public static final int REQUEST_CODE = 420;
    private Uri imgUri;
    public static final String FB_STORAGE_PATH = "image/";
    public static final String FB_DATABASE_PATH = "image/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_settings);
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

    public void photoPicker(View view){
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUri = data.getData();

            try {
                Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);

                /* get phone resolution */
                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager wm = (WindowManager) getApplicationContext().
                        getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;
                Bitmap b2 = Bitmap.createScaledBitmap(bm, screenWidth, screenHeight, false);

                /* get read external storage permission during runtime to get
                access to the gallery
                */

                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]
                                    {Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE);
                    return;
                }

                // get course location access permission for accessing location
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSION_ACCESS_COURSE_LOCATION);
                    return;
                }

                // get fine location access permission for accessing location
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSION_ACCESS_FINE_LOCATION);
                    return;
                }

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]
                                    {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    return;
                }


                /* get other album directory and write to it */

                final String dirName = "DejaPhotoCopied";
                //final File imageRoot = new File(Environment.getExternalStoragePublicDirectory
                //        (Environment.DIRECTORY_PICTURES), dirName);
                final File imageRoot = new File(Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_PICTURES), dirName);
                //imageRoot.delete();
                if(!imageRoot.exists())
                    imageRoot.mkdirs();
                final File image = new File(imageRoot, imgUri.getLastPathSegment());
                //if(image.exists())
                //    image.delete();

                /*
                String path = Environment.getExternalStoragePublicDirectory().toString();
                //File path = Environment.getExternalStoragePublicDirectory();
                File image = new File(path, "image1.jpg");
                if (!image.exists()) {
                    image.mkdirs();
                }*/
                //image.createNewFile();
                OutputStream fOutputStream = new FileOutputStream(image);
                b2.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);
                fOutputStream.flush();
                fOutputStream.close();
                MediaStore.Images.Media.insertImage(getContentResolver(), image.getAbsolutePath(),
                        image.getName(), image.getName());

                Toast.makeText(getApplicationContext(),"Made image", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /*@Override
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

    }*/

    public void choosePhotos_btn(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_CODE);
    }


}
