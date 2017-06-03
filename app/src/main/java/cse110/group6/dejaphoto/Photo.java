package cse110.group6.dejaphoto;


import android.location.Location;
import android.net.Uri;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Michael on 5/6/2017.
 */

public class Photo implements Serializable{
    String filePath;
    double longitude;
    double latitude;
    Date dateTaken;
    int karma;
    boolean released;
    boolean shared;
    double weight;
    String uriLastPathSegment;
    private static final long millisecondsInDay = 86400000;
    private static final long millisecondsInWeek = 7 * millisecondsInDay;
    private static final long millisecondsInMonth = 30 * millisecondsInDay;

    /* constructor */
    public Photo(String filePath, double longitude, double latitude, Date dateTaken, int karma,
                 boolean released, boolean shared, double weight, String uriLastPathSegment) {
        this.filePath = filePath;
        this.longitude = longitude;
        this.latitude = latitude;
        this.dateTaken = dateTaken;
        this.karma = karma;
        this.released = released;
        this.shared = shared;
        this.weight = weight;
        this.uriLastPathSegment = uriLastPathSegment;
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

    public void setDateTaken(Date dateTaken) {
        this.dateTaken = dateTaken;
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

    public Date getDateTaken() {
        return dateTaken;
    }

    public boolean isReleased() {
        return released;
    }

    public int getKarma() { return karma; }

    public void setKarma(int karma) { this.karma = karma; }

    public String getUriLastPathSegment() { return uriLastPathSegment; }

    public void setUriLastPathSegment(String uriLastPathSegment) { this.uriLastPathSegment = uriLastPathSegment; }

    public boolean isShared() { return shared;}

    public void setShared(boolean shared) { this.shared = shared; }





    // Recalculates weight for current status
    void calcWeight(Location location) {
        Calendar calendar = Calendar.getInstance();
        int currDay = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Date class is 0-indexed, Calendar is 1-indexed
        int currHour = calendar.get(Calendar.HOUR_OF_DAY);
        long currTime = calendar.getTime().getTime();

        weight = 0;
        float[] result = new float[1];
        Location.distanceBetween(this.latitude, this.longitude, location.getLatitude(), location.getLongitude(), result);
        float radius = result[0];

        //location
        if(radius < 500)
            weight += 100;
        else if(radius < 2000){
            weight += 50;
        }else if(radius < 5000){
            weight += 25;
        }

        //karma
        if(karma > 0){
            weight += 75;
        }

        //recency
        long photoTimeTaken = dateTaken.getTime();
        if(currTime - photoTimeTaken < millisecondsInDay){
            weight += 50;
        }else if(currTime - photoTimeTaken < millisecondsInWeek){
            weight += 20;
        }else if(currTime - photoTimeTaken < millisecondsInMonth){
            weight += 10;
        }


        //day
        int photoDayOfWeek = dateTaken.getDay();
        if(currDay == photoDayOfWeek)
            weight += 25;

        //time
        int photoHourOfDay = dateTaken.getHours();
        int diffTime = Math.abs(photoHourOfDay - currHour);
        if(diffTime == 0)
            weight += 25;
        if(diffTime < 3 || diffTime > 21)
            weight += 15;
    }

    // Accessor method for weight
    double getWeight() {
        return weight;
    }
}
