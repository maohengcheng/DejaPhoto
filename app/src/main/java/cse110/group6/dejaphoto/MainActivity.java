package cse110.group6.dejaphoto;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import static android.graphics.BitmapFactory.decodeFile;
import static cse110.group6.dejaphoto.R.mipmap.ic_karma_gray;
import static cse110.group6.dejaphoto.R.mipmap.ic_release;
import static cse110.group6.dejaphoto.R.mipmap.ic_undo;
import static java.lang.Boolean.FALSE;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 20;
    private static int RESULT_LOAD_IMG = 1;
    private ImageView imageView;
    private String imageLoc;
    int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;
    int MY_PERMISSION_ACCESS_COURSE_LOCATION = 5042;
    int MY_PERMISSION_ACCESS_FINE_LOCATION = 5048;
    int screenWidth;
    int screenHeight;
    PhotoAlbum photos;
    SwipeListener swipeListener;
    Bitmap bitmap;
    File imageFile;

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
        // the results will be higher than using the activity context object or the getWindowManager() shortcut

        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

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
        photos.setCursor(getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                photos.getImages(), null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"));
        imageLoc = photos.getMostRecentImage();
        if(imageLoc != null) {
            imageFile = new File(imageLoc);
            setImageView(imageLoc, imageView, imageFile);
        } else {
            Toast.makeText(this, "No image", Toast.LENGTH_SHORT).show();
        }
        photos.initializePhotos();

        /*--------------------------------------------------------
        //BackgroundService Call
        //-Creates an intent called otherIntent
        //-we use putExtra to passed in variables to the service
        //
        //-------------------------------------------------------*/

        Intent otherIntent = new Intent(MainActivity.this, BackgroundService.class);

        otherIntent.putExtra("filepaths", photos.getPhotos()); // passing in the whole vector of photos
        //otherIntent.putExtra("photoPos", photos.getCursor().getPosition()); // passing in the position of the current photo in the vector of photos

        //Call the service to run in the background
        startService(otherIntent);




        /* swipe left and right code adapted from:
            http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures */
        /* instantiate the swipeListener Object, then set imageView
            to have this listener */
        swipeListener = new SwipeListener(MainActivity.this) {
            public void onSwipeRight() {
                imageLoc = photos.getPrevImage();
                if(imageLoc != null) {
                    imageFile = new File(imageLoc);
                    setImageView(imageLoc, imageView, imageFile);
                    setButtons(imageView);
                } else {
                    Toast.makeText(MainActivity.this, "No previous image",
                            Toast.LENGTH_SHORT).show();
                }
            }
            public void onSwipeLeft() {
                imageLoc = photos.getNextImage();
                if(imageLoc != null) {
                    imageFile = new File(imageLoc);
                    setImageView(imageLoc, imageView, imageFile);
                    setButtons(imageView);
                } else {
                    Toast.makeText(MainActivity.this, "No next image",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        new Thread() {
            public void run() {
                imageView.setOnTouchListener(swipeListener);
            }
        }.start();

    }
    /* end of onCreate */

    /*
    @Override
    protected void onPause(){
        super.onPause();
        Intent intent = new Intent(MainActivity.this, SetBackground.class);
        intent.putExtra("filepath", imageLoc); // passing in just a string, the images filepath
        //List<Photo> tempPhotos = new ArrayList<Photo>();
        //tempPhotos = photos.getPhotos();
        //intent.putExtra("filepaths", photos.getPhotos()); // passing in the whole vector of photos
        //intent.putExtra("photoPos", photos.getCursor().getPosition()); // passing in the position of the current photo in the vector of photos
        startService(intent);

        //bitmap = decodeSampledBitmap(imageLoc, imageFile, screenWidth, screenHeight);
        /* bJPGcompress adapted from:
            http://android.okhelp.cz/compressing-a-bitmap-to-jpg-format-android-example/ */
        // Best of quality is 80 and more, 3 is very low quality of image
        ////Bitmap bJPGcompress = codec(bitmap, Bitmap.CompressFormat.JPEG, 1);
        //Bitmap bitmap = BitmapFactory.decodeFile(imageLoc);

        /*check if image exists, then set imageView and background */
        /*
        if(imageFile.exists() && imageLoc != null) {
            imageView.setImageBitmap(bitmap);

                WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(getApplicationContext());

            try {
                    myWallpaperManager.setBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    //}

    @Override
    protected void onStop(){
        super.onStop();
        Intent intent = new Intent(MainActivity.this, SetBackground.class);
        intent.putExtra("filepath", imageLoc); // passing in just a string, the images filepath


        startService(intent);



        //bitmap = decodeFile(imageLoc);
        //bitmap = decodeSampledBitmap(imageLoc, imageFile, screenWidth, screenHeight);
        /* bJPGcompress adapted from:
            http://android.okhelp.cz/compressing-a-bitmap-to-jpg-format-android-example/ */
        // Best of quality is 80 and more, 3 is very low quality of image
        ////Bitmap bJPGcompress = codec(bitmap, Bitmap.CompressFormat.JPEG, 1);
        //Bitmap bitmap = BitmapFactory.decodeFile(imageLoc);

        /*check if image exists, then set imageView and background */
        /*
        if(imageFile.exists() && imageLoc != null) {
            imageView.setImageBitmap(bitmap);

            WallpaperManager myWallpaperManager
                    = WallpaperManager.getInstance(getApplicationContext());

            try {
                myWallpaperManager.setBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    /* functions calculateInSampleSize and decodeSampleBitmap adapted from:
        https://developer.android.com/topic/performance/graphics/load-bitmap.html */
    /* calculates the new height and width of a picture if they exceed the
        bounds
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        /* get height and width of image */
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        /* if dimensions exceed bounds, resize */
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /* Decode a bitmap while adjusting it width and height if they exceed
        the bounds
     */
    public static Bitmap decodeSampledBitmap(String imageLoc, File imageFile,
                                             int reqWidth, int reqHeight) {
        /* decode while checking image dimensions */
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeFile(imageLoc, options);

        /* calculate inSampleSize */
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        /* decode without checking image dimensions */
        options.inJustDecodeBounds = false;
        return decodeFile(imageLoc, options);
    }

    /* function codec adapted from:
    http://android.okhelp.cz/compressing-a-bitmap-to-jpg-format-android-example/ */
    /* reformats an image to jpeg format */
    private static Bitmap codec(Bitmap src, Bitmap.CompressFormat format,
                                int quality) {
        /* get the image data */
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        /* convert image */
        src.compress(format, quality, os);
        byte[] array = os.toByteArray();
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }

    /* sets the apps imageView and the phones background to some image
        specified by the image's file location
     */
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public synchronized boolean setImageView(String imageLoc,
                                             ImageView imageView, File imageFile) {
        //final Bitmap bitmap =
        //     decodeSampledBitmap(imageLoc, imageFile, screenWidth, screenHeight);

        bitmap = decodeFile(imageLoc);
        /* bJPGcompress adapted from:
            http://android.okhelp.cz/compressing-a-bitmap-to-jpg-format-android-example/ */
        // Best of quality is 80 and more, 3 is very low quality of image
        ////Bitmap bJPGcompress = codec(bitmap, Bitmap.CompressFormat.JPEG, 1);
        //Bitmap bitmap = BitmapFactory.decodeFile(imageLoc);

        /*check if image exists, then set imageView and background */
        if (imageFile.exists() && imageLoc != null) {
            imageView.setImageBitmap(bitmap);
            return true;
            /* set the background in a separate thread to improve app runtime
                speed
                */
            /*
            Thread set = new Thread() {
                WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(getApplicationContext());

                public void run() {
                        //Return if the thread is interrupted
                        if(Thread.interrupted()) {
                            return;
                        }
                        else {
                            try {
                                myWallpaperManager.setBitmap(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                }
            };
            set.start();*/

            /*
            if(Thread.getAllStackTraces().keySet().size() > 2) {
                for (Thread t : Thread.getAllStackTraces().keySet()) {
                    t.interrupt();
                    System.gc();
                }
            }
            */
            //int nbThreads = Thread.getAllStackTraces().keySet().size();
            /*int nbRunning = 0;
            for(Thread t : Thread.getAllStackTraces().keySet()) {
                if (t.getState() == Thread.State.RUNNABLE)
                    nbRunning++;
            }
            if(nbRunning > 3) {
                for(Thread t : Thread.getAllStackTraces().keySet()) {
                    t.interrupt();
                    nbRunning--;
                }
            }*/
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult
            (int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! do the
                    // calendar task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
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
        //super.onActivityResult(requestCode, resultCode, data);
        try {
            if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && data != null) {

                /* get image data with a uri */
                Uri photoUri = data.getData();
                /* get actual image from uri */
                photos.setCursor(getContentResolver().query(photoUri,
                        photos.getImages(), null, null, null));
                imageLoc = photos.getMostRecentImage();
                File imageFile = new File(imageLoc);
                setImageView(imageLoc, imageView, imageFile);
            } else {
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // What to do when settings button is pressed (launch activity)
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* function called for when the karma button is pressed */
    public void giveKarma(View view) {
        ImageButton button = (ImageButton) findViewById(R.id.karmaButton);
        button.setImageResource(R.mipmap.ic_karma);

        //get current images position in photoalbum
        int photoPos = photos.getCursor().getPosition();
        Photo karmaPhoto = photos.getPhotos().get(photoPos);
        // set current photo's karma to true
        karmaPhoto.setKarma(true);
        //calculate the weight of the current photo now
        //karmaPhoto.calcWeight(); its ok we got it

        Toast.makeText(this, karmaPhoto.getFilePath() + " has been given " +
                "good karma!", Toast.LENGTH_SHORT).show();
    }

    /* function called for when the release button is pressed */
    public void releasePhoto(View view) {
        ImageButton button = (ImageButton) findViewById(R.id.releaseButton);
        int photoPos = photos.getCursor().getPosition();
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
    public void setButtons(View view){
        ImageButton karmaButton = (ImageButton) findViewById(R.id.karmaButton);
        ImageButton releaseButton = (ImageButton) findViewById(R.id.releaseButton);
        int photoPos = photos.getCursor().getPosition();
        Photo currPhoto = photos.getPhotos().get(photoPos);

        /* set the karma buttona for this picture to the correct icon */
        if (currPhoto.isKarma()){
            karmaButton.setImageResource(R.mipmap.ic_karma);
        }
        else{
            karmaButton.setImageResource(ic_karma_gray);
        }

        /* set the release button for this picture to the correct icon */
        if(currPhoto.isReleased()){
            releaseButton.setImageResource(ic_undo);
        }
        else{
            releaseButton.setImageResource(ic_release);
        }
    }
}
