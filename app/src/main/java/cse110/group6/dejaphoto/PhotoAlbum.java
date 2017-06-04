package cse110.group6.dejaphoto;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by stevennatalius on 5/3/17.
 */

public class PhotoAlbum implements Serializable{
    String[] projectImage;
    Cursor cursor;
    int filePathIndex;
    Vector<Photo> photos;


    /* constructor */
    public PhotoAlbum() {
        projectImage = new String[] {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.LONGITUDE,
                MediaStore.Images.ImageColumns.LATITUDE,
        };
        filePathIndex = 1;
    }

    /* initialize the vector of photos */
    public void initializePhotos(){
        int origPos = cursor.getPosition();
        cursor.moveToFirst();
        photos = new Vector<Photo>(10, 5);

        for(int i = 0; i < cursor.getCount(); i++){
            String filePath = cursor.getString(1);
            double longitude = cursor.getDouble(5);
            double latitude = cursor.getDouble(6);
            Date dateTaken = new Date(cursor.getLong(3) * 1000);
            Uri thisUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + cursor.getInt(0));
            //String namer = (cursor.getString(1)).substring(cursor.getString(1).lastIndexOf("/") + 1);
            photos.add(new Photo(filePath, longitude, latitude, dateTaken, 0, false, true , 0, thisUri.getLastPathSegment()));
            //System.out.println("id: " + filePath + " date: " + dateTaken + " long: " + longitude + " lat: " + latitude);
            //System.out.println("filepath from photos: " + photos.get(i).getFilePath());
            cursor.moveToNext();
        }
        cursor.moveToPosition(origPos);
    }

    /* setters */
    public void setCursor (Cursor currentCursor) {
        cursor = currentCursor;
    }

    /* getters */
    public String[] getImages() {
        return projectImage;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public String getImage(int index) {
        if(cursor.moveToPosition(index)) {
            return cursor.getString(filePathIndex);
        } else
            return null;
    }

    /* functions that will get some image's filepath */
    public String getMostRecentImage () {
        if(cursor.moveToFirst()) {
            return cursor.getString(filePathIndex);
        }
        else
            return null;
    }

    public String getNextImage() {
        if(cursor.moveToNext()) {
            return cursor.getString(filePathIndex);
        } else
            return null;
    }

    public String getPrevImage() {
        if(cursor.moveToPrevious()) {
            return cursor.getString(filePathIndex);
        } else
            return null;
    }

    public String searchImage(String path){

        for (int i = 0; i < photos.size(); i++)
        {
            if (photos.get(i).getFilePath().equals(path))
                return photos.get(i).getFilePath();
            else
                return null;
        }

        return cursor.getString(filePathIndex);
    }

    public void closeCursor() {
        cursor.close();
    }

    public Vector<Photo> getPhotos() { return photos; }

    public static void saveToCustomDirectory(Context context, Context appContext, ContentResolver resolver,
                                      Uri imgUri, String dirName) throws IOException {
        Bitmap bm = MediaStore.Images.Media.getBitmap(resolver, imgUri);

                /* get phone resolution */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) appContext.
                getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        Bitmap b2 = Bitmap.createScaledBitmap(bm, screenWidth, screenHeight, false);
    /* get other album directory and write to it */
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

        MediaScannerConnection.scanFile(context, new String[]{image.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("External Storage", "Scanned " + path + ":");
                        Log.i("External Storage", "-> uri=" + uri);
                    }
                });

        Toast.makeText(appContext,"Image Copied", Toast.LENGTH_SHORT).show();
    }
}
