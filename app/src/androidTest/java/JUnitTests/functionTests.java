package JUnitTests;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import cse110.group6.dejaphoto.MainActivity;
import cse110.group6.dejaphoto.Photo;
import cse110.group6.dejaphoto.PhotoAlbum;
import cse110.group6.dejaphoto.R;

import static cse110.group6.dejaphoto.R.id.karmaButton;
import static cse110.group6.dejaphoto.R.mipmap.ic_karma;
import static cse110.group6.dejaphoto.R.mipmap.ic_karma_gray;
import static cse110.group6.dejaphoto.R.mipmap.ic_notsharing;
import static cse110.group6.dejaphoto.R.mipmap.ic_release;
import static cse110.group6.dejaphoto.R.mipmap.ic_sharing;
import static cse110.group6.dejaphoto.R.mipmap.ic_undo;
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
        /* check that a valid file will let the method run its course */
        boolean check = mainActivity.getActivity().setImageView(imageLoc, imageView, imageFile);
        assertEquals(true, check);

        /* check that an invalid file will cause the method to throw an exception */
        check = mainActivity.getActivity().setImageView(null, imageView, imageFile);
        assertEquals(false, check);
    }

    @UiThreadTest
    @Test
    public void giveKarmaTest(){
        PhotoAlbum tempPhotos = photos;
        photos = mainActivity.getActivity().getPhotos();
        int photoPos = photos.getCursor().getPosition();

        /* test that karma is initially false for a photo */
        Photo karmaPhoto = photos.getPhotos().get(photoPos);
        karmaPhoto.setKarma(0);
        assertEquals(0, karmaPhoto.getKarma());

        ImageButton testButton = (ImageButton) mainActivity.getActivity().findViewById(R.id.karmaButton);

        /* test that karma is now true for a photo after a click */
        testButton.performClick();
        karmaPhoto = photos.getPhotos().get(photoPos);
        assertEquals(1, karmaPhoto.getKarma());

        /* test that karma is still true for a photo after another click */
        testButton.performClick();
        karmaPhoto = photos.getPhotos().get(photoPos);
        assertEquals(2, karmaPhoto.getKarma());

        photos = tempPhotos;
    }

    @UiThreadTest
    @Test
    public void toggleReleasePhotoTest(){
        PhotoAlbum tempPhotos = photos;
        photos = mainActivity.getActivity().getPhotos();
        int photoPos = photos.getCursor().getPosition();

        /* test that released is initially false for a photo */
        Photo releasePhoto = photos.getPhotos().get(photoPos);
        assertEquals(false, releasePhoto.isReleased());

        ImageButton testButton = (ImageButton) mainActivity.getActivity().findViewById(R.id.releaseButton);

        /* test that after a click, the photo is now release */
        testButton.performClick();
        releasePhoto = photos.getPhotos().get(photoPos);
        assertEquals(true, releasePhoto.isReleased());

        /* test that after another click, the photo is now not released
            again
         */
        testButton.performClick();
        releasePhoto = photos.getPhotos().get(photoPos);
        assertEquals(false, releasePhoto.isReleased());

        photos = tempPhotos;
    }

    @UiThreadTest
    @Test
    public void toggleSharePhotoTest() {
        PhotoAlbum tempPhotos = photos;
        photos = mainActivity.getActivity().getPhotos();
        int photoPos = photos.getCursor().getPosition();

        /* test that shared is initially true for a photo */
        Photo sharePhoto = photos.getPhotos().get(photoPos);
        assertEquals(true, sharePhoto.isShared());

        ImageButton testButton = (ImageButton) mainActivity.getActivity().findViewById(R.id.shareButton);

        /* test that after a click, the photo is no longer shared */
        testButton.performClick();
        sharePhoto = photos.getPhotos().get(photoPos);
        assertEquals(false, sharePhoto.isShared());

        /* test that after another click, the photo is shared again */
        testButton.performClick();
        sharePhoto = photos.getPhotos().get(photoPos);
        assertEquals(true, sharePhoto.isShared());

        photos = tempPhotos;
    }

    @UiThreadTest
    @Test
    public void setButtonsTest(){
        ImageButton karmaButton = (ImageButton) mainActivity.getActivity().findViewById(R.id.karmaButton);
        ImageButton releaseButton = (ImageButton) mainActivity.getActivity().findViewById(R.id.releaseButton);
        ImageButton shareButton = (ImageButton) mainActivity.getActivity().findViewById(R.id.shareButton);

        /* photo is initially not karmad, not released, and shared.
            buttons should initially be karma_gray, not released, and shared icons.
        */
        PhotoAlbum tempPhotos = photos;
        photos = mainActivity.getActivity().getPhotos();
        int photoPos = photos.getCursor().getPosition();

        /* test that karma is initially false for a photo,
            release button should be release icon,
            and share button is initially shared icon
         */
        Photo karmaPhoto = photos.getPhotos().get(photoPos);
        karmaButton.setTag(ic_karma_gray);
        releaseButton.setTag(ic_release);
        shareButton.setTag(ic_sharing);
        mainActivity.getActivity().setButtons(karmaPhoto);
        assertEquals(ic_karma_gray, (int)karmaButton.getTag());
        assertEquals(ic_release, (int)releaseButton.getTag());
        assertEquals(ic_sharing, (int)shareButton.getTag());

        /* the photo is now karmad and released.
            buttons should now be karma gray, undo, and not shared icons.
         */
        karmaButton.performClick();
        releaseButton.performClick();
        shareButton.performClick();
        mainActivity.getActivity().setButtons(karmaPhoto);
        assertEquals(ic_karma_gray, (int)karmaButton.getTag());
        assertEquals(ic_undo, (int)releaseButton.getTag());
        assertEquals(ic_notsharing, (int)shareButton.getTag());

        /* the photo is now karmad and not released.
            buttons should now be karma, release, and shared icons.
         */
        karmaButton.performClick();
        releaseButton.performClick();
        shareButton.performClick();
        mainActivity.getActivity().setButtons(karmaPhoto);
        assertEquals(ic_karma, (int)karmaButton.getTag());
        assertEquals(ic_release, (int)releaseButton.getTag());
        assertEquals(ic_sharing, (int)shareButton.getTag());
    }

    @UiThreadTest
    @Test
    public void nextImageTest(){
        PhotoAlbum tempPhotos = photos;
        int tempCursorPos = photos.getCursor().getPosition();
        photos = mainActivity.getActivity().getPhotos();

        /* test that the cursor moves correctly when the nextImage method
            is called
         */
        photos.getCursor().moveToPosition(0);
        mainActivity.getActivity().nextImage(imageView);
        assertEquals(1, photos.getCursor().getPosition());
        mainActivity.getActivity().nextImage(imageView);
        assertEquals(2, photos.getCursor().getPosition());
        mainActivity.getActivity().nextImage(imageView);
        assertEquals(3, photos.getCursor().getPosition());

        photos = tempPhotos;
        photos.getCursor().moveToPosition(tempCursorPos);
    }

    @UiThreadTest
    @Test
    public void prevImageTest(){
        PhotoAlbum tempPhotos = photos;
        int tempCursorPos = photos.getCursor().getPosition();
        photos = mainActivity.getActivity().getPhotos();

        /* test that the cursor moves correctly when the prevImage method
            is called
         */
        photos.getCursor().moveToPosition(3);
        mainActivity.getActivity().prevImage(imageView);
        assertEquals(2, photos.getCursor().getPosition());
        mainActivity.getActivity().prevImage(imageView);
        assertEquals(1, photos.getCursor().getPosition());
        mainActivity.getActivity().prevImage(imageView);
        assertEquals(0, photos.getCursor().getPosition());

        photos = tempPhotos;
        photos.getCursor().moveToPosition(tempCursorPos);
    }
}
