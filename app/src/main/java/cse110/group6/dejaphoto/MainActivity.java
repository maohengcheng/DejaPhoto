/* Spring 2017 CSE 110
 * Group 6
 * Names:
 *      Mao-Heng Michael Cheng
 *      Kenneth Ashly
 *      Julius Guzman
 *      Junnel Reboquio
 *      Steven Natalius
 *      Alex Lui
 */
package cse110.group6.dejaphoto;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.graphics.BitmapFactory.decodeFile;
import static cse110.group6.dejaphoto.R.mipmap.ic_karma;
import static cse110.group6.dejaphoto.R.mipmap.ic_karma_gray;
import static cse110.group6.dejaphoto.R.mipmap.ic_release;
import static cse110.group6.dejaphoto.R.mipmap.ic_undo;
import static java.lang.Boolean.FALSE;

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

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 20;
    private static int RESULT_LOAD_IMG = 1;
    private static int RESULT_SETTINGS = 2;
    private ImageView imageView;
    private String imageLoc;
    int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;
    int MY_PERMISSION_ACCESS_COURSE_LOCATION = 5042;
    int MY_PERMISSION_ACCESS_FINE_LOCATION = 5048;
    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 421;
    int screenWidth;
    int screenHeight;
    int photoPos;
    PhotoAlbum photos;
    SwipeListener swipeListener;
    Bitmap bitmap;
    File imageFile;
    public static long backgroundInterval = 10000; //10seconds default
    Intent otherIntent;
    public static final String FB_STORAGE_PATH = "image/";
    public static final String FB_DATABASE_PATH = "image/";
    public static final String IMAGE_FOLDER_REF = "Images";

    public static final int REQUEST_CODE = 420;
    private Uri imgUri;
    String dirName;
    File imageRoot;
    String selection;
    String[] selectionArgs;

    private StorageReference mStorageRef;
    FirebaseUser user;
    private DatabaseReference mDatabaseRef;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        dirName = "DejaPhotoCamera";
        //final File imageRoot = new File(Environment.getExternalStoragePublicDirectory
        //        (Environment.DIRECTORY_PICTURES), dirName);
        imageRoot = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES), File.separator + dirName);
        //imageRoot.delete();
        if(!imageRoot.exists())
            imageRoot.mkdirs();

        dirName = "DejaPhotoFriends";
        //final File imageRoot = new File(Environment.getExternalStoragePublicDirectory
        //        (Environment.DIRECTORY_PICTURES), dirName);
        imageRoot = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES), File.separator + dirName);
        //imageRoot.delete();
        if(!imageRoot.exists())
            imageRoot.mkdirs();

        dirName = "DejaPhotoCopied";
        //final File imageRoot = new File(Environment.getExternalStoragePublicDirectory
        //        (Environment.DIRECTORY_PICTURES), dirName);
        imageRoot = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES), File.separator + dirName);
        //imageRoot.delete();
        if(!imageRoot.exists())
            imageRoot.mkdirs();

        System.out.println(imageRoot.toString());

        /* get the firebase database references */
        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(user.getUid() + "/" + IMAGE_FOLDER_REF);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.mainView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* get phone resolution */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().
                getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        /* permission requests during runtime code adapted from:
            http://stackoverflow.com/questions/37441133/how-i-can-request-
                permission-at-runtime-in-android
         */
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

        if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return;
        }


        /* instantiate the PhotoAlbum object, then initialize it first with the
            most recent image in the gallery */
        photos = new PhotoAlbum();

        /* swipe left and right code adapted from:
            http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures */
        /* instantiate the swipeListener Object, then set imageView
            to have this listener */
        swipeListener = new SwipeListener(MainActivity.this) {
            /* on right swipe, get previous image and set app's view
                to that image
            */
            public void onSwipeRight() {
                imageLoc = photos.getPrevImage();
                if(imageLoc != null) {
                    photoPos = photos.getCursor().getPosition();
                    Photo currPhoto = photos.getPhotos().get(photoPos);
                    imageFile = new File(imageLoc);

                    setImageView(imageLoc, imageView, imageFile);
                    setButtons(currPhoto);
                    updateLocationDisplay(currPhoto);
                    Toast.makeText(MainActivity.this, "Swiped Right", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No previous image",
                            Toast.LENGTH_SHORT).show();
                    photos.cursor.moveToFirst();
                    imageLoc = photos.getImage(photos.getCursor().getPosition());
                }
            }
            /* on left swipe, get next image and set app's view
                to that image
             */
            public void onSwipeLeft() {
                imageLoc = photos.getNextImage();
                if(imageLoc != null) {
                    photoPos = photos.getCursor().getPosition();
                    Photo currPhoto = photos.getPhotos().get(photoPos);
                    imageFile = new File(imageLoc);

                    setImageView(imageLoc, imageView, imageFile);
                    setButtons(currPhoto);
                    updateLocationDisplay(currPhoto);
                    Toast.makeText(MainActivity.this, "Swiped Left", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "No next image",
                            Toast.LENGTH_SHORT).show();
                    photos.cursor.moveToLast();
                    imageLoc = photos.getImage(photos.getCursor().getPosition());
                }
            }
        };
        /* run the swipeListener on a separate thread */
        new Thread() {
            public void run() {
                imageView.setOnTouchListener(swipeListener);
            }
        }.start();

        /*Local Receiver for managing data from services */
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("intentKey"));
    }
    /* end of onCreate */

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        //selection=MediaStore.Video.Media.DATA +" like?";
        selection=MediaStore.Images.Media.DATA +" like?";
        //selectionArgs=new String[]{"%DejaPhotoCopied%"};
        selectionArgs=new String[]{"%DejaPhotoCopied%"};

        /*
        photos.setCursor(getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        photos.getImages(), null, null,
                        MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"));
        */
        photos.setCursor(getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        photos.getImages(), selection, selectionArgs,
                        MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"));

        imageLoc = photos.getMostRecentImage();
        if(imageLoc != null) {
            imageFile = new File(imageLoc);
            setImageView(imageLoc, imageView, imageFile);
            photos.initializePhotos();
            photoPos = photos.getCursor().getPosition();
            Photo currPhoto = photos.getPhotos().get(photoPos);




            // Create listener to get image data from database
            //mDatabaseRef.addValueEventListener(new ValueEventListener() {
            mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(Photo i : photos.photos) {
                        for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                            if (imageSnapshot.child("name").getValue(String.class).equals(i.getUriLastPathSegment())){
                                i.setUriLastPathSegment(imageSnapshot.child("name").getValue(String.class));
                                i.setKarma(imageSnapshot.child("karma").getValue(Integer.class));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

            setButtons(currPhoto);
            updateLocationDisplay(currPhoto);
        } else {
            Toast.makeText(this, "No image. Please select an image", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        stopService(otherIntent);
    }

    /* the app, upon losing focus and not being on phone's foreground anymore,
        will set the phone's wallpaper to the last viewed image
     */
    @Override
    protected void onStop(){
        super.onStop();

        if(imageLoc != null) {

            photoPos = photos.getCursor().getPosition();
            imageLoc = photos.getImage(photoPos);

            //   Intent intent = new Intent(MainActivity.this, SetBackground.class);
            //  intent.putExtra("filepath", imageLoc); // passing in just a string, the images filepath
            //  startService(intent);

        /*--------------------------------------------------------
        //BackgroundService Call
        //-Creates an intent called otherIntent
        //-we use putExtra to passed in variables to the service
        //-------------------------------------------------------*/

            otherIntent = new Intent(MainActivity.this, BackgroundService.class);
            otherIntent.putExtra("filepath", imageLoc); //passing in just a string of image filepath
            otherIntent.putExtra("filepaths", photos.getPhotos()); // passing in the whole vector of photos
            otherIntent.putExtra("Interval", backgroundInterval); // passing in the time interval
            //Call the service to run in the background
            startService(otherIntent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(otherIntent);
    }

    /* sets the apps imageView and the phones background to some image
        specified by the image's file location
     */
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public synchronized boolean setImageView(String imageLoc,
                                             ImageView imageView, File imageFile) {
        bitmap = decodeFile(imageLoc);

        /*check if image exists, then set imageView and background */
        if (imageFile.exists() && imageLoc != null) {
            imageView.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult
            (int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted

                } else {

                    // permission denied
                }
                return;
            }
            // other 'switch' lines to check for other
            // permissions this app might request
        }
    }

    /* loads an image from as a response to the "load image" button */
    public void loadImage(View view) {
        /* create intent for getting images through the album */
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        /* start the intent */
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    /* get next image */
    @TargetApi(Build.VERSION_CODES.M)
    public void nextImage(View view) {
        imageLoc = photos.getNextImage();
        if(imageLoc != null) {
            File imageFile = new File(imageLoc);
            setImageView(imageLoc, imageView, imageFile);
        } else {
            Toast.makeText(this, "No next image", Toast.LENGTH_SHORT).show();
        }
    }

    /* get previous image */
    @TargetApi(Build.VERSION_CODES.M)
    public void prevImage(View view) {
        imageLoc = photos.getPrevImage();
        if(imageLoc != null) {
            File imageFile = new File(imageLoc);
            setImageView(imageLoc, imageView, imageFile);
        } else {
            Toast.makeText(this, "No previous image", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    /* gets the selected image's data and sets it to imageView and the
        background
     */
    @SuppressWarnings("VisibleForTests")
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            /*if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && data != null) {

                //* get image data with a uri *//*
                Uri photoUri = data.getData();
                //* get actual image from uri *//*
                photos.setCursor(getContentResolver().query(photoUri,
                        photos.getImages(), null, null, null));
                imageLoc = photos.getMostRecentImage();
                File imageFile = new File(imageLoc);
                setImageView(imageLoc, imageView, imageFile);
            }*/
            if(requestCode == RESULT_SETTINGS && resultCode == RESULT_OK) {
                backgroundInterval = data.getLongExtra("newtime", backgroundInterval);
                String backgroundIntervalString = Long.toString((backgroundInterval));
                Toast.makeText(this, backgroundIntervalString, Toast.LENGTH_LONG).show();
            }
            /*else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_LONG).show();
            }*/
        } catch (Exception e) {
            Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // What to do when toolbar buttons are pressed (launch activity)
        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent startActivity = new Intent(this, Settings.class);
                startActivityForResult(startActivity, RESULT_SETTINGS);

                return true;

            case R.id.action_friends_list:
                Intent startFriendsActivity = new Intent(this, FriendsList.class);
                startActivity(startFriendsActivity);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("key");
            String imagePath = intent.getStringExtra("currPath");
            File imageFile = new File(imagePath);
            setImageView(imagePath, imageView, imageFile);
        }
    };

    /* function called for when the karma button is pressed */
    public void giveKarma(View view) {
        ImageButton button = (ImageButton) findViewById(R.id.karmaButton);
        button.setImageResource(R.mipmap.ic_karma);

        //get current images position in photoalbum
        photoPos = photos.getCursor().getPosition();
        final Photo karmaPhoto = photos.getPhotos().get(photoPos);
        // set current photo's karma to true
        final int currKarma = karmaPhoto.getKarma();
        karmaPhoto.setKarma(currKarma + 1);

        TextView karmaview = (TextView) findViewById(R.id.karmaDisplay);
        karmaview.setText("Karma: " + karmaPhoto.getKarma());

        //mDatabaseRef.addValueEventListener(new ValueEventListener() {
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                        if (imageSnapshot.child("name").getValue(String.class).equals(karmaPhoto.getUriLastPathSegment())){
                            Map<String, Object> update = new HashMap<String, Object>();
                            update.put(karmaPhoto.getUriLastPathSegment(), new ImageUpload(karmaPhoto.getUriLastPathSegment(),
                                    imageSnapshot.child("url").getValue(String.class), currKarma + 1,
                                    imageSnapshot.child("shared").getValue(boolean.class)));
                            mDatabaseRef.updateChildren(update);
                        }
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        Toast.makeText(this, karmaPhoto.getFilePath() + " has been given " +
                "good karma! It now has " + karmaPhoto.getKarma() + " karma!"
                , Toast.LENGTH_SHORT).show();
    }

    /* function called for when the release button is pressed */
    public void toggleReleasePhoto(View view) {
        ImageButton button = (ImageButton) findViewById(R.id.releaseButton);
        photoPos = photos.getCursor().getPosition();
        final Photo releasePhoto = photos.getPhotos().get(photoPos);

        // check if photo was released or not, then act accordingly by setting
        // the released flag to the opposite boolean value
        if(releasePhoto.isReleased()) {
            button.setImageResource(R.mipmap.ic_release);
            releasePhoto.setReleased(false);
            Toast.makeText(this, releasePhoto.getFilePath() + " is no longer " +
                    "released", Toast.LENGTH_SHORT).show();
        }
        else {
            button.setImageResource(R.mipmap.ic_undo);
            releasePhoto.setReleased(true);
            releasePhoto.setKarma(0);
            Toast.makeText(this, releasePhoto.getFilePath() + "PhotoAlbum " +
                    "is released", Toast.LENGTH_SHORT).show();

            TextView karmaview = (TextView) findViewById(R.id.karmaDisplay);
            karmaview.setText("Karma: " + releasePhoto.getKarma());

            // Create listener to get images, then update the appropriate images' karma
            //mDatabaseRef.addValueEventListener(new ValueEventListener() {
            mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                            if (imageSnapshot.child("name").getValue(String.class).equals(releasePhoto.getUriLastPathSegment())){
                                Map<String, Object> update = new HashMap<String, Object>();
                                update.put(releasePhoto.getUriLastPathSegment(), new ImageUpload(releasePhoto.getUriLastPathSegment(),
                                        imageSnapshot.child("url").getValue(String.class), 0,
                                        imageSnapshot.child("shared").getValue(boolean.class)));
                                mDatabaseRef.updateChildren(update);
                            }
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    /* function called when the share button is pressed */
    public void toggleSharePhoto(View view) {
        ImageButton button = (ImageButton) findViewById(R.id.shareButton);
        button.setImageResource(R.mipmap.ic_notsharing);
    }

    /* function to update the karma and release buttons to the correct icon */
    public void setButtons(Photo photo){
        ImageButton karmaButton = (ImageButton) findViewById(R.id.karmaButton);
        ImageButton releaseButton = (ImageButton) findViewById(R.id.releaseButton);

        TextView karmaview = (TextView) findViewById(R.id.karmaDisplay);
        karmaview.setText("Karma: " + photo.getKarma());

        /* set the karma buttona for this picture to the correct icon */
        if (photo.getKarma() > 0){
            karmaButton.setImageResource(R.mipmap.ic_karma);
            karmaButton.setTag(ic_karma);
        }
        else{
            karmaButton.setImageResource(ic_karma_gray);
            karmaButton.setTag(ic_karma_gray);
        }

        /* set the release button for this picture to the correct icon */
        if(photo.isReleased()){
            releaseButton.setImageResource(ic_undo);
            releaseButton.setTag(ic_undo);
        }
        else{
            releaseButton.setImageResource(ic_release);
            releaseButton.setTag(ic_release);
        }
    }

    public void updateLocationDisplay(Photo photo) {
        TextView locationDisplay = (TextView) findViewById(R.id.locationDisplay);

        // source: http://stackoverflow.com/questions/6922312/get-location-name-from-fetched-coordinates
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(photo.getLatitude(), photo.getLongitude(), 1);
            if(null != listAddresses && listAddresses.size() > 0)
                locationDisplay.setText(listAddresses.get(0).getAddressLine(0));
            else
                locationDisplay.setText("Unknown Location");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public PhotoAlbum getPhotos() {
        return photos;
    }
}
