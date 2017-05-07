package cse110.group6.dejaphoto;

/**
 * Created by Michael on 5/6/2017.
 */

public class Photo {
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
    }
}
