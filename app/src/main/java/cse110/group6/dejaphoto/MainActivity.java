package cse110.group6.dejaphoto;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
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
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static android.graphics.BitmapFactory.decodeFile;
import static java.lang.Boolean.FALSE;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 20;
    private static int RESULT_LOAD_IMG = 1;
    private ImageView imageView;
    private String imageLoc;
    int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;
    int screenWidth;
    int screenHeight;
    PhotoAlbum photos;
    SwipeListener swipeListener;

    private static boolean released = FALSE; // temporary field for testing release button

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
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        /* get read external storage permission during runtime to get
            access to the gallery
         */
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE);
            return;
        }

        /* instantiate the PhotoAlbum object, then initialize it first with the
            most recent image in the gallery */
        photos = new PhotoAlbum();
        photos.setCursor(getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                photos.getImages(), null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"));
        imageLoc = photos.getMostRecentImage();
        if(imageLoc != null) {
            File imageFile = new File(imageLoc);
            setBackgroundAndView(imageLoc, imageView, imageFile);
        } else {
            Toast.makeText(this, "No image", Toast.LENGTH_SHORT).show();
        }
        photos.initializePhotos();

        /* swipe left and right code adapted from:
            http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures */
        /* instantiate the swipeListener Object, then set imageView
            to have this listener */
        swipeListener = new SwipeListener(MainActivity.this) {
            public void onSwipeRight() {
                imageLoc = photos.getPrevImage();
                if(imageLoc != null) {
                    File imageFile = new File(imageLoc);
                    setBackgroundAndView(imageLoc, imageView, imageFile);
                } else {
                    Toast.makeText(MainActivity.this, "No previous image", Toast.LENGTH_SHORT).show();
                }
            }
            public void onSwipeLeft() {
                imageLoc = photos.getNextImage();
                if(imageLoc != null) {
                    File imageFile = new File(imageLoc);
                    setBackgroundAndView(imageLoc, imageView, imageFile);
                } else {
                    Toast.makeText(MainActivity.this, "No next image", Toast.LENGTH_SHORT).show();
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
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

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
    private void setBackgroundAndView(String imageLoc, ImageView imageView, File imageFile) {
        ////final Bitmap bitmap =
        ////        decodeSampledBitmap(imageLoc, imageFile, screenWidth, screenHeight);

        final Bitmap bitmap = decodeFile(imageLoc);
        /* bJPGcompress adapted from:
            http://android.okhelp.cz/compressing-a-bitmap-to-jpg-format-android-example/ */
        // Best of quality is 80 and more, 3 is very low quality of image
        ////Bitmap bJPGcompress = codec(bitmap, Bitmap.CompressFormat.JPEG, 1);
        //Bitmap bitmap = BitmapFactory.decodeFile(imageLoc);

        /*check if image exists, then set imageView and background */
        if(imageFile.exists() && imageLoc != null) {
            imageView.setImageBitmap(bitmap);

            /* set the background in a separate thread to improve app runtime
                speed
                */
            new Thread() {
                WallpaperManager myWallpaperManager
                        = WallpaperManager.getInstance(getApplicationContext());
                final Bitmap bitmapThread = bitmap;
                public void run() {
                    try {
                        myWallpaperManager.setBitmap(bitmapThread);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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
            setBackgroundAndView(imageLoc, imageView, imageFile);
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
            setBackgroundAndView(imageLoc, imageView, imageFile);
        } else {
            Toast.makeText(this, "No previous image", Toast.LENGTH_SHORT).show();
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
                photos.setCursor(getContentResolver().query(photoUri, photos.getImages(), null, null, null));
                imageLoc = photos.getMostRecentImage();
                File imageFile = new File(imageLoc);
                setBackgroundAndView(imageLoc, imageView, imageFile);
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_LONG).show();
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

    public void giveKarma(View view) {
        ImageButton button = (ImageButton) findViewById(R.id.karmaButton);
        button.setImageResource(R.mipmap.ic_karma);
        Toast.makeText(this, "PhotoAlbum has been given good karma!", Toast.LENGTH_SHORT).show();
    }

    public void releasePhoto(View view) {
        ImageButton button = (ImageButton) findViewById(R.id.releaseButton);

        if(released) {
            button.setImageResource(R.mipmap.ic_release);
            Toast.makeText(this, "PhotoAlbum is no longer released", Toast.LENGTH_SHORT).show();
        }
        else {
            button.setImageResource(R.mipmap.ic_undo);
            Toast.makeText(this, "PhotoAlbum is released", Toast.LENGTH_SHORT).show();
        }

        released = !released;
    }
}
