package cse110.group6.dejaphoto;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import static android.graphics.BitmapFactory.decodeFile;

public class BackgroundService extends Service {

    public static long interval = 0;
    private Handler mHandler = new Handler();
    private Timer mTimer=null;
    private Location currLocation = new Location("default"); // initializes location at (0, 0)
    private Location lastUpdatedLocation = new Location("default");
    private LocationManager mLocationManager=null;
    private ArrayList<Photo> photoAlbum;
    private String currFilePath;
    boolean firstRun = true;
    private static final long LOCATION_REFRESH_TIME = 1000; // time in milliseconds
    private static final float LOCATION_REFRESH_DISTANCE = 50; // distance in meters
    private static final float BACKGROUND_UPDATE_DISTANCE = 250;

    // does this do anything? - kenny/alex
    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

       // filePath = intent.getStringExtra("filepath");

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){

        if(mTimer!= null)
            mTimer.cancel();

        else
            mTimer=new Timer();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"Unable to Access Location", Toast.LENGTH_SHORT).show();
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.

        //use getExtra to get the values we passed with intent from mainActivity
        photoAlbum = (ArrayList<Photo>) intent.getExtras().getSerializable("filepaths");
        //photoPos = intent.getIntExtra("photoPos", 0);
        interval = intent.getLongExtra("Interval",0);


        /* get the images filepath and then set the background */
        String filePath = intent.getStringExtra("filepath");
        Bitmap bitmap = decodeFile(filePath);
        File imageFile = new File(filePath);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)
                getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        bitmap = bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true);

        if(imageFile.exists() && filePath != null) {
            WallpaperManager myWallpaperManager
                    = WallpaperManager.getInstance(getApplicationContext());

            try {
                myWallpaperManager.setBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(firstRun == true) {
            synchronized (this) {
                try {
                    wait(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            firstRun = false;
        }


        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(),0,interval);

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Toast.makeText(this, "BackService Closed", Toast.LENGTH_LONG).show();
        sendMessageToActivity("I'm from BackService");
        mTimer.cancel();


    }

    // source: http://stackoverflow.com/questions/17591147/how-to-get-current-location-in-android
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}
        @Override
        public void onProviderEnabled(String s) {}
        @Override
        public void onProviderDisabled(String s) {}
    };

    // update location and check if background should be changed
    void updateLocation(Location location) {
        currLocation = location;

        float[] distance = new float[1];
        Location.distanceBetween(currLocation.getLatitude(), currLocation.getLongitude(), lastUpdatedLocation.getLatitude(), lastUpdatedLocation.getLongitude(), distance);
        if(distance[0] > BACKGROUND_UPDATE_DISTANCE)
            updateBackground();
    }

    //inner class of TimeDisplayTimerTask
    private class TimeDisplayTimerTask extends TimerTask {
        @Override
        public void run(){
            //run a thread
            mHandler.post(new Runnable(){
                // Called every interval
                @Override
                public void run() {
                    updateBackground();
                }
            });
        }
    }

    void updateBackground() {
        ArrayList<Photo> photoAlbumCopy = (ArrayList<Photo>) photoAlbum.clone();

        for(int i = 0; i < photoAlbumCopy.size();) {
            if(photoAlbumCopy.get(i).isReleased())
                photoAlbumCopy.remove(i);
            else
                i++;
        }

        Photo[] photos = new Photo[photoAlbumCopy.size()];
        photos = photoAlbumCopy.toArray(photos);

        // Calculate current weight
        for (Photo photo : photos)
            photo.calcWeight(currLocation);

        setBackground(choosePhoto(photos).getFilePath());

        lastUpdatedLocation = currLocation;
    }

    /* get the images filepath and then set the background */
    void setBackground(String filePath) {

        currFilePath = filePath;
        Bitmap bitmap = decodeFile(filePath);
        File imageFile = new File(filePath);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)
                getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        bitmap = bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true);

        if(imageFile.exists() && filePath != null) {
            WallpaperManager myWallpaperManager
                    = WallpaperManager.getInstance(getApplicationContext());

            try {
                myWallpaperManager.setBitmap(bitmap);
                Toast.makeText(this, "Background changed", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("intentKey");
// You can also include some extra data.
        intent.putExtra("key", msg);
        intent.putExtra("currPath", currFilePath);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    // Choose photo algorithm, move to service class when made
    // This assumes that all photos are not released and can be chosen
    @TargetApi(Build.VERSION_CODES.M)
    static Photo choosePhoto(Photo[] photos) {
        if(photos.length > 4) {
            // Initialize data structures
            ArrayList<Photo> otherPhotos = new ArrayList<Photo>(photos.length);
            Photo[] topPhotos = {photos[0], photos[1], photos[2], photos[3]};
            sortPhotos(topPhotos);

            // Sort photos into top 4 and other
            for(int i = 4; i < photos.length; i++) {
                if(photos[i].getWeight() > topPhotos[3].getWeight()) {
                    otherPhotos.add(topPhotos[3]);
                    insertPhoto(topPhotos, photos[i]);
                }
                else
                    otherPhotos.add(photos[i]);
            }

            // Choose photo: [40%, 30%, 15%, 10%], 5%
            int randomInt = ThreadLocalRandom.current().nextInt(0, 100);

            if(randomInt < 40)
                return topPhotos[0];

            if(randomInt < 70)
                return topPhotos[1];

            if(randomInt < 85)
                return topPhotos[2];

            if(randomInt < 95)
                return topPhotos[3];

            randomInt = ThreadLocalRandom.current().nextInt(0, otherPhotos.size());
            return otherPhotos.get(randomInt);

        }
        else {
            Photo[] otherPhotos = new Photo[photos.length];
            System.arraycopy(photos, 0, otherPhotos, 0, photos.length);
            sortPhotos(otherPhotos);

            // Choose photo (60% pick first, 40% pick random including first)
            int randomInt = ThreadLocalRandom.current().nextInt(0, 5);
            if(randomInt < 3)
                return otherPhotos[0];

            randomInt = ThreadLocalRandom.current().nextInt(0, photos.length);
            return otherPhotos[randomInt];
        }
    }

    // Insertion sort, descending order
    private static void sortPhotos(Photo[] photos) {
        for(int i = 1; i < photos.length; i++) {
            for(int j = i; j > 0 && photos[j-1].getWeight() < photos[j].getWeight(); j--) {
                Photo temp = photos[j];
                photos[j] = photos[j-1];
                photos[j-1] = temp;
            }
        }
    }

    // Insert photo into array of ascending order by weight, overwriting smallest element
    private static void insertPhoto(Photo[] photos, Photo photo) {
        for(int i = photos.length - 1; i > 0; i--) {
            if(photo.getWeight() > photos[i-1].getWeight())
                photos[i] = photos[i-1];
            else {
                photos[i] = photo;
                return;
            }
        }
        photos[0] = photo;
    }
}
