package cse110.group6.dejaphoto;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by stevennatalius on 5/3/17.
 */

public class Photo {
    String[] projectImage;
    Cursor cursor;
    double weight;
    boolean isReleased;
    final double MAGIC_NUMBER = 10.0;
    int columnIndex;
    String imageLoc;

    Photo() {
        projectImage = new String[] {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE,
        };
        isReleased = false;
        columnIndex = 1;
        weight = MAGIC_NUMBER;
    }

    public String[] getImages() {
        return projectImage;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor (Cursor currentCursor) {
        cursor = currentCursor;
    }

    public String getMostRecentImage () {
        new Thread() {
            public void run() {
                try {
                    cursor.moveToFirst();
                    imageLoc = cursor.getString(columnIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return imageLoc;
        /*
        if(cursor.moveToFirst()) {
            return cursor.getString(columnIndex);
        }
        else
            return null;*/
    }

    public String getNextImage() {
        new Thread() {
            public void run() {
                try {
                    cursor.moveToNext();
                    imageLoc = cursor.getString(columnIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return imageLoc;
    }

    public String getPrevImage() {
        new Thread() {
            public void run() {
                try {
                    cursor.moveToPrevious();
                    imageLoc = cursor.getString(columnIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return imageLoc;
        /*if(cursor.moveToPrevious()) {
            return cursor.getString(columnIndex);
        } else
            return null; */
    }

    public void closeCursor() {
        cursor.close();
    }



}
