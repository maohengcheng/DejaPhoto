package cse110.group6.dejaphoto;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static android.graphics.BitmapFactory.decodeFile;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 20;
    private static int RESULT_LOAD_IMG = 1;
    private ImageView imageView;
    private String imageLoc;
    int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;
    int screenWidth;
    int screenHeight;
    Photo photos;

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

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE is an
            // app-defined int constant

            return;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        photos = new Photo();
        photos.setCursor(getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                photos.getImages(), null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"));

        imageLoc = photos.getMostRecentImage();
        if(imageLoc != null) {
            File imageFile = new File(imageLoc);
            setBackgroundAndView(imageLoc, imageView, imageFile);
        } else {
            Toast.makeText(this, "No image", Toast.LENGTH_SHORT).show();
        }

    }





    /* functions calculateInSampleSize and decodeSampleBitmap adapted from:
        https://developer.android.com/topic/performance/graphics/load-bitmap.html */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmap(String imageLoc, File imageFile,
                                             int reqWidth, int reqHeight) {

        // Decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeFile(imageLoc, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return decodeFile(imageLoc, options);
    }

    /* function codec adapted from:
    http://android.okhelp.cz/compressing-a-bitmap-to-jpg-format-android-example/ */
    private static Bitmap codec(Bitmap src, Bitmap.CompressFormat format,
                                int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(format, quality, os);

        byte[] array = os.toByteArray();
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }





    private void setBackgroundAndView(String imageLoc, ImageView imageView, File imageFile) {
        ////final Bitmap bitmap =
        ////        decodeSampledBitmap(imageLoc, imageFile, screenWidth, screenHeight);


        final Bitmap bitmap = decodeFile(imageLoc);

        /* bJPGcompress adapted from:
            http://android.okhelp.cz/compressing-a-bitmap-to-jpg-format-android-example/ */
        // Best of quality is 80 and more, 3 is very low quality of image
        ////Bitmap bJPGcompress = codec(bitmap, Bitmap.CompressFormat.JPEG, 1);
        //Bitmap bitmap = BitmapFactory.decodeFile(imageLoc);
        if(imageFile.exists() && imageLoc != null) {
            imageView.setImageBitmap(bitmap);

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

    public void loadImage(View view) {

        //Create intent for getting photos through the album
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //Start the intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    public void nextImage(View view) {
        imageLoc = photos.getNextImage();
        if(imageLoc != null) {
            File imageFile = new File(imageLoc);
            setBackgroundAndView(imageLoc, imageView, imageFile);
        } else {
            Toast.makeText(this, "No next image", Toast.LENGTH_SHORT).show();
        }

    }

    public void prevImage(View view) {
        imageLoc = photos.getPrevImage();
        if(imageLoc != null) {
            File imageFile = new File(imageLoc);
            setBackgroundAndView(imageLoc, imageView, imageFile);
        } else {
            Toast.makeText(this, "No previous image", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        try {
            if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && data != null) {

                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
