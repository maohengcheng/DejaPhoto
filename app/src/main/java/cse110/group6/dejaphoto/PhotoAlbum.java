package cse110.group6.dejaphoto;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

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

}
