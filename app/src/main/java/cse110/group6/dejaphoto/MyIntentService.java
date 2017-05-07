package cse110.group6.dejaphoto;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actionsgit, extra parameters and static
 * helper methods.
 */
public class MyIntentService extends IntentService {
    //from stackoverflow.com/questions/15754195/android-toast-message-every-1-minute
    public static final long INTERVAL = 15000;
    private Handler mHandler = new Handler();
    private Timer mTimer=null;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "cse110.group6.dejaphoto.action.FOO";
    private static final String ACTION_BAZ = "cse110.group6.dejaphoto.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "cse110.group6.dejaphoto.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "cse110.group6.dejaphoto.extra.PARAM2";

    public MyIntentService() {
        super("MyIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * onStartCommand
     *
     *
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(MyIntentService.this, "Service Started", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * onDestroy
     *
     *
     */
    @Override
    public void onDestroy(){
        Toast.makeText(MyIntentService.this, "Service Stopped", Toast.LENGTH_SHORT).show();
        mTimer.cancel();
        super.onDestroy();
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            while (true) {


                synchronized (this) {
                    try {
                        wait(5000);
                        if (mTimer != null)
                            mTimer.cancel();
                        else
                            mTimer = new Timer();

                        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, INTERVAL); //schedule the task

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //stopService(intent);
                }

            }

        }
    }

    //inner class of TimeDisplayTimerTask
    private class TimeDisplayTimerTask extends TimerTask{
        @Override
        public void run(){
            mHandler.post(new Runnable() {
                @Override
                public void run(){
                    //display toast
                    Toast.makeText(getApplicationContext(), "Message here?", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
