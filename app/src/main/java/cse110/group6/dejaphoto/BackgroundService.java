package cse110.group6.dejaphoto;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {

    //from stackoverflow.com/questions/15754195/android-toast-message-every-1-minute
    public static final long INTERVAL = 15000;
    private Handler mHandler = new Handler();
    private Timer mTimer=null;

    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        if(mTimer!= null)
            mTimer.cancel();

        else
            mTimer=new Timer();

        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(),0,INTERVAL);
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
                    Toast.makeText(getApplicationContext(),"Boop", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
