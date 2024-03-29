package cse110.group6.dejaphoto;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import static android.graphics.BitmapFactory.decodeFile;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SetBackground extends IntentService{
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "cse110.group6.dejaphoto.action.FOO";
    private static final String ACTION_BAZ = "cse110.group6.dejaphoto.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "cse110.group6.dejaphoto.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "cse110.group6.dejaphoto.extra.PARAM2";

    public SetBackground() {
        super("SetBackground");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(SetBackground.this, "Set Background", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(SetBackground.this, "Service Stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SetBackground.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SetBackground.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            synchronized (this) {
                /* below block of code if for getting the vector of photos */
                /* to be used in the BackgroundService class, which will pass a single string to this service to set the background */
                //try {
                //List<Photo> photos = (List<Photo>) intent.getExtras().getSerializable("filepaths");
                /*
                if(objects != null) {
                    String a = objects.getARr
                }*/
                //int photoPos = intent.getIntExtra("photoPos", 0);
                //Photo photo = photos.get(photoPos);
                //String filePath = photo.getFilePath();


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
                stopService(intent);
            }
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
