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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.graphics.BitmapFactory.decodeFile;
import static cse110.group6.dejaphoto.R.mipmap.ic_karma;
import static cse110.group6.dejaphoto.R.mipmap.ic_karma_gray;
import static cse110.group6.dejaphoto.R.mipmap.ic_release;
import static cse110.group6.dejaphoto.R.mipmap.ic_undo;
import static java.lang.Boolean.FALSE;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 20;
    private static int RESULT_LOAD_IMG = 1;
    private static int RESULT_SETTINGS = 2;
    private ImageView imageView;
    private String imageLoc;
    int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;
    int MY_PERMISSION_ACCESS_COURSE_LOCATION = 5042;
    int MY_PERMISSION_ACCESS_FINE_LOCATION = 5048;
    int screenWidth;
    int screenHeight;
    int photoPos;
    PhotoAlbum photos;
    SwipeListener swipeListener;
    Bitmap bitmap;
    File imageFile;
    public static long backgroundInterval = 10000; //10seconds default
    Intent otherIntent;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                } else {
                    Toast.makeText(MainActivity.this, "No previous image",
                            Toast.LENGTH_SHORT).show();
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
                } else {
                    Toast.makeText(MainActivity.this, "No next image",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        /* run the swipeListener on a separate thread */
        new Thread() {
            public void run() {
                imageView.setOnTouchListener(swipeListener);
            }
        }.start();

    }
    /* end of onCreate */

    @Override
    protected void onStart() {
        super.onStart();

        photos.setCursor(getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        photos.getImages(), null, null,
                        MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"));
        imageLoc = photos.getMostRecentImage();
        if(imageLoc != null) {
            imageFile = new File(imageLoc);
            setImageView(imageLoc, imageView, imageFile);
            photos.initializePhotos();
            photoPos = photos.getCursor().getPosition();
            Photo currPhoto = photos.getPhotos().get(photoPos);
            updateLocationDisplay(currPhoto);
        } else {
            Toast.makeText(this, "No image", Toast.LENGTH_SHORT).show();
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

        photoPos = photos.getCursor().getPosition();
        imageLoc = photos.getImage(photoPos);

        Intent intent = new Intent(MainActivity.this, SetBackground.class);
        intent.putExtra("filepath", imageLoc); // passing in just a string, the images filepath

        startService(intent);

        /*--------------------------------------------------------
        //BackgroundService Call
        //-Creates an intent called otherIntent
        //-we use putExtra to passed in variables to the service
        //-------------------------------------------------------*/

        otherIntent = new Intent(MainActivity.this, BackgroundService.class);
        otherIntent.putExtra("filepaths", photos.getPhotos()); // passing in the whole vector of photos
        otherIntent.putExtra("Interval", backgroundInterval); // passing in the position of the current photo in the vector of photos
        //Call the service to run in the background
        startService(otherIntent);

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
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && data != null) {

                //* get image data with a uri *//*
                Uri photoUri = data.getData();
                //* get actual image from uri *//*
                photos.setCursor(getContentResolver().query(photoUri,
                        photos.getImages(), null, null, null));
                imageLoc = photos.getMostRecentImage();
                File imageFile = new File(imageLoc);
                setImageView(imageLoc, imageView, imageFile);
            }
               if(requestCode == RESULT_SETTINGS && resultCode == RESULT_OK) {
                   backgroundInterval = data.getLongExtra("newtime", backgroundInterval);
                   String backgroundIntervalString = Long.toString((backgroundInterval));
                   Toast.makeText(this, backgroundIntervalString, Toast.LENGTH_LONG).show();
               }

                else {
                Toast.makeText(this, "No image selected",
                        Toast.LENGTH_LONG).show();
            }
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

        // What to do when settings button is pressed (launch activity)
        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent startActivity = new Intent(this, Settings.class);
                startActivityForResult(startActivity, RESULT_SETTINGS);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* function called for when the karma button is pressed */
    public void giveKarma(View view) {
        ImageButton button = (ImageButton) findViewById(R.id.karmaButton);
        button.setImageResource(R.mipmap.ic_karma);

        //get current images position in photoalbum
        photoPos = photos.getCursor().getPosition();
        Photo karmaPhoto = photos.getPhotos().get(photoPos);
        // set current photo's karma to true
        karmaPhoto.setKarma(true);

        Toast.makeText(this, karmaPhoto.getFilePath() + " has been given " +
                "good karma!", Toast.LENGTH_SHORT).show();
    }

    /* function called for when the release button is pressed */
    public void toggleReleasePhoto(View view) {
        ImageButton button = (ImageButton) findViewById(R.id.releaseButton);
        photoPos = photos.getCursor().getPosition();
        Photo releasePhoto = photos.getPhotos().get(photoPos);

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
            Toast.makeText(this, releasePhoto.getFilePath() + "PhotoAlbum " +
                    "is released", Toast.LENGTH_SHORT).show();
        }
    }

    /* function to update the karma and release buttons to the correct icon */
    public void setButtons(Photo photo){
        ImageButton karmaButton = (ImageButton) findViewById(R.id.karmaButton);
        ImageButton releaseButton = (ImageButton) findViewById(R.id.releaseButton);

        /* set the karma buttona for this picture to the correct icon */
        if (photo.isKarma()){
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
