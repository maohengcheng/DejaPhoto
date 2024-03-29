package cse110.group6.dejaphoto;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Switch;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class Settings extends AppCompatActivity implements OnItemSelectedListener {
    public static final String FB_STORAGE_PATH = "image/";
    public static final String FB_DATABASE_PATH = "image/";
    public static final String IMAGE_FOLDER_REF = "Images";
    public static final String SETTINGS_PREF = "Settings Preferences";

    public static final String YOUR_PHOTOS_KEY = "View Your Photos";
    public static final String FRIENDS_PHOTOS_KEY = "View Friends Photos";
    public static final String SHARE_PHOTOS_KEY = "Share Photos";

    public static final String LOCATION_KEY = "Use Location";
    public static final String DOW_KEY = "Use Day of Week";
    public static final String TOD_KEY = "Use Time of Day";
    public static final String RECENT_KEY = "Use Recent Photos";
    public static final String KARMA_KEY = "Use Karma";

    Spinner spinnerDialog;
    ArrayAdapter adapter;

    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 421;
    int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;
    int MY_PERMISSION_ACCESS_COURSE_LOCATION = 5042;
    int MY_PERMISSION_ACCESS_FINE_LOCATION = 5048;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 20;
    public static final int REQUEST_CODE = 420;
    private Uri imgUri;

    SharedPreferences settingsFile;
    SharedPreferences.Editor settingsEditor;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_settings);
        getSupportActionBar().setTitle("Settings");

        adapter = ArrayAdapter.createFromResource(this, R.array.time_entries, android.R.layout.simple_spinner_item);

        spinnerDialog = (Spinner) findViewById(R.id.spinner_dialog);
        spinnerDialog.setAdapter(adapter);
        spinnerDialog.setOnItemSelectedListener(Settings.this);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(user.getDisplayName() + "/" + IMAGE_FOLDER_REF);

        settingsFile = getSharedPreferences(SETTINGS_PREF, 0);
        settingsEditor = settingsFile.edit();

        Switch viewYourPhotosState = (Switch) findViewById(R.id.ownPhotosSwitch);
        viewYourPhotosState.setChecked(settingsFile.getBoolean(YOUR_PHOTOS_KEY, true));
        viewYourPhotosState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsEditor.putBoolean(YOUR_PHOTOS_KEY, isChecked);
                settingsEditor.commit();
            }
        });

        Switch viewFriendsPhotosState = (Switch) findViewById(R.id.friendsPhotosSwitch);
        viewFriendsPhotosState.setChecked(settingsFile.getBoolean(FRIENDS_PHOTOS_KEY, true));
        viewFriendsPhotosState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsEditor.putBoolean(FRIENDS_PHOTOS_KEY, isChecked);
                settingsEditor.commit();
            }
        });

        Switch sharePhotosState = (Switch) findViewById(R.id.sharePhotosSwitch);
        sharePhotosState.setChecked(settingsFile.getBoolean(SHARE_PHOTOS_KEY, true));
        sharePhotosState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsEditor.putBoolean(SHARE_PHOTOS_KEY, isChecked);
                settingsEditor.commit();
            }
        });

        Switch locationState = (Switch) findViewById(R.id.locationSwitch);
        locationState.setChecked(settingsFile.getBoolean(LOCATION_KEY, true));
        locationState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsEditor.putBoolean(LOCATION_KEY, isChecked);
                settingsEditor.commit();
            }
        });


        Switch dayOfWeekState = (Switch) findViewById(R.id.dowSwitch);
        dayOfWeekState.setChecked(settingsFile.getBoolean(DOW_KEY, true));
        dayOfWeekState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                settingsEditor.putBoolean(DOW_KEY, isChecked);
                settingsEditor.commit();
            }
        });


        Switch timeOfDayState = (Switch) findViewById(R.id.todSwitch);
        timeOfDayState.setChecked(settingsFile.getBoolean(TOD_KEY, true));
        timeOfDayState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsEditor.putBoolean(TOD_KEY, isChecked);
                settingsEditor.commit();
            }
        });

        Switch recentState = (Switch) findViewById(R.id.recentSwitch);
        recentState.setChecked(settingsFile.getBoolean(RECENT_KEY, true));
        recentState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsEditor.putBoolean(RECENT_KEY, isChecked);
                settingsEditor.commit();
            }
        });


        Switch karmaState = (Switch) findViewById(R.id.karmaSwitch);
        karmaState.setChecked(settingsFile.getBoolean(KARMA_KEY, true));
        karmaState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsEditor.putBoolean(KARMA_KEY, isChecked);
                settingsEditor.commit();
            }
        });
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

    public String getImageExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void photoPicker(View view){
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE);
    }


    @SuppressWarnings("VisibleForTests")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final PhotoAlbum tempAlbum = new PhotoAlbum();
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUri = data.getData();

            try {
                /*
                Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
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

                tempAlbum.saveToCustomDirectory(this, getApplicationContext(), getContentResolver(),
                        imgUri, "DejaPhotoCopied");

                if (imgUri !=null) {
                    final ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setTitle("Uploading image");
                    dialog.show();
                    StorageReference ref = mStorageRef.child(FB_STORAGE_PATH + System.currentTimeMillis() +
                            "." + getImageExt(imgUri));
                    //Add file
                    ref.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Dismiss dialog when done
                            dialog.dismiss();
                            //Save image info into firebase database
                            //String uploadId = mDatabaseRef.push().getKey();
                            String uploadId = tempAlbum.temp;
                            ImageUpload imageUpload = new ImageUpload(tempAlbum.temp,
                                    taskSnapshot.getDownloadUrl().toString(), 0, true);
                            mDatabaseRef.child(uploadId).setValue(imageUpload);
                            Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Dismiss dialog and show an error
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    //Show upload progress
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    dialog.setMessage("Uploaded " + (int)progress+"0");
                                }
                            });
                }else {
                    Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    /*
    public void saveToCustomDirectory(Bitmap b2, String dirName) throws IOException {
    /* get other album directory and write to it
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        //final File imageRoot = new File(Environment.getExternalStoragePublicDirectory
        //        (Environment.DIRECTORY_PICTURES), dirName);
        //content://media/external/images/media
        final File imageRoot = new File(root, File.separator + dirName);

        //imageRoot.delete();
        if(!imageRoot.exists()) {
            imageRoot.mkdirs();
        }
        final File image = new File(imageRoot, imgUri.getLastPathSegment() + ".jpg");
        if(image.exists())
            image.delete();

        FileOutputStream fOutputStream = new FileOutputStream(image);
        b2.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);
        fOutputStream.flush();
        fOutputStream.close();

        MediaScannerConnection.scanFile(this, new String[]{image.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("External Storage", "Scanned " + path + ":");
                        Log.i("External Storage", "-> uri=" + uri);
                    }
                });

        Toast.makeText(getApplicationContext(),"Image Copied", Toast.LENGTH_SHORT).show();
    } */

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
