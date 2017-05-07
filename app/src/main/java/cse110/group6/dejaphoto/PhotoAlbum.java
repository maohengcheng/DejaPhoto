package cse110.group6.dejaphoto;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import java.util.*;

/**
 * Created by stevennatalius on 5/3/17.
 */

public class PhotoAlbum {
    String[] projectImage;
    Cursor cursor;
    int filePathIndex;
    int weight;
    String imageLoc;
    Vector<Photo> photos;

    /* constructor */
    PhotoAlbum() {
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
            long dateTaken = cursor.getLong(3);
            photos.add(new Photo(filePath, longitude, latitude, dateTaken, false, false, 0));
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

    public void closeCursor() {
        cursor.close();
    }
}