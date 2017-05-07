package cse110.group6.dejaphoto;

<<<<<<< HEAD
=======
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

>>>>>>> 052f5e8e078b373e2527822ba87564d7dfe0e573
/**
 * Created by Michael on 5/6/2017.
 */

public class Photo {
<<<<<<< HEAD
    String filePath;
    double longitude;
    double latitude;
    long dateTaken;
    boolean karma;
    boolean released;
    double weight;

    /* constructor */
    public Photo(String filePath, double longitude, double latitude, long dateTaken, boolean karma, boolean released, double weight) {
        this.filePath = filePath;
        this.longitude = longitude;
        this.latitude = latitude;
        this.dateTaken = dateTaken;
        this.karma = karma;
        this.released = released;
        this.weight = weight;
=======
    String[] projectImage;
    Cursor cursor;
    int weight;
    boolean isReleased;
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
        weight = 0;
>>>>>>> 052f5e8e078b373e2527822ba87564d7dfe0e573
    }

    /* setters */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
    }

    public void setKarma(boolean karma) {
        this.karma = karma;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

<<<<<<< HEAD
    /* getters */
    public String getFilePath() {
        return filePath;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public long getDateTaken() {
        return dateTaken;
    }

    public boolean isKarma() {
        return karma;
    }

    public boolean isReleased() {
        return released;
    }

    public double getWeight() {
        return weight;
=======
    // Recalculates weight for current status (may need fields)
    void calcWeight() {
        // Calculate difference in location, add some corresponding value to weight

        // Calculate difference in time of day

        // Calculate difference in day of the week

        // Karma
    }

    // Accessor method for weight
    int getWeight() {
        return weight;
    }

    // Choose photo algorithm, move to service class when made
    // This assumes that all photos are not released and can be chosen
    static Photo choosePhoto(Photo[] photos) {
        for(Photo photo : photos)
            photo.calcWeight();

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
>>>>>>> 052f5e8e078b373e2527822ba87564d7dfe0e573
    }
}
