package cse110.group6.dejaphoto;

/**
 * Created by Michael on 6/2/2017.
 */

public class ImageUpload {
    public String name;
    public String url;
    public int karma;
    public  boolean shared;

    public int getKarma() {
        return karma;
    }

    public String getName(){
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isShared(){
        return shared;
    }

    public ImageUpload(String name, String url, int karma, boolean shared) {
        this.name = name;
        this.url = url;
        this.karma = karma;
        this.shared = shared;
    }
}
