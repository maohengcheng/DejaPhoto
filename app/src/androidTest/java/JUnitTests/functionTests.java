package JUnitTests;

import android.database.Cursor;
import android.media.MediaActionSound;
import android.provider.MediaStore;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.widget.ImageView;
import android.widget.Toast;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import cse110.group6.dejaphoto.MainActivity;
import cse110.group6.dejaphoto.PhotoAlbum;
import cse110.group6.dejaphoto.R;

import static junit.framework.Assert.assertEquals;

/**
 * Created by stevennatalius on 5/11/17.
 */

public class functionTests {
    PhotoAlbum photos;
    String imageLoc;
    File imageFile;
    ImageView imageView;

    @Rule
    public ActivityTestRule<MainActivity> mainActivity =
            new ActivityTestRule<MainActivity>(MainActivity.class);

    @Before
    public void setUp() {
                /* instantiate the PhotoAlbum object, then initialize it first with the
            most recent image in the gallery */
        photos = new PhotoAlbum();
        imageView = (ImageView) mainActivity.getActivity().findViewById(R.id.mainView);
        photos.setCursor(mainActivity.getActivity().getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        photos.getImages(), null, null,
                        MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"));
        imageLoc = photos.getMostRecentImage();
        if(imageLoc != null) {
            imageFile = new File(imageLoc);
        }
        photos.initializePhotos();
    }

    @UiThreadTest
    @Test
    public void setImageViewTest() {
        boolean check = mainActivity.getActivity().setImageView(imageLoc, imageView, imageFile);
        assertEquals(true, check);
        check = mainActivity.getActivity().setImageView(null, imageView, imageFile);
        assertEquals(false, check);
    }

    @Test
    public void testPhotoAlbum() {
        Cursor check = (mainActivity.getActivity().getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        photos.getImages(), null, null,
                        MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"));

        assertEquals(photos.getCursor(), check);
    }
}
