package cse110.group6.dejaphoto;

import android.Manifest;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static android.graphics.BitmapFactory.decodeFile;

public class BackgroundService extends Service {

    //from stackoverflow.com/questions/15754195/android-toast-message-every-1-minute
    public static final long INTERVAL = 15000;
    private Handler mHandler = new Handler();
    private Timer mTimer=null;
    private List<Photo> photos;
    private int photoPos;
    private Photo photo;
    private String filePath;
    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        filePath = intent.getStringExtra("filepath");

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){

        if(mTimer!= null)
            mTimer.cancel();

        else
            mTimer=new Timer();

           }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.

        filePath = intent.getStringExtra("filepath");

        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(),0,INTERVAL);

        return START_STICKY;
    }

    @Override
    public void onDestroy(){

        Toast.makeText(this, "Destroyed", Toast.LENGTH_LONG).show();
        mTimer.cancel();
    }

    //inner class of TimeDisplayTimerTask
    private class TimeDisplayTimerTask extends TimerTask {
        @Override
        public void run(){
            //run a thread
            mHandler.post(new Runnable(){
                @Override
                public void run(){
                    //display a Toast every 20secs I think
                    //String name = "Bwahhhh";

            //        String filePath = photo.getFilePath();
                    Toast.makeText(getApplicationContext(),filePath, Toast.LENGTH_SHORT).show();

                /* get the images filepath and then set the background */

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

                }
            });
        }
    }
}
