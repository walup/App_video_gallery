package com.example.mricinema3;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Video{
    private String videoURL;
    private String videoName;
    private Bitmap frameImage;
    private int videoIndex;
    private float duration;
    private final static int WIDTH = 200;
    private final static int HEIGHT = 150;

    public Video(String videoURL, String videoName, Bitmap bitmap, float duration, int videoIndex){
        this.videoURL = videoURL;
        this.videoName = videoName;
        this.duration = duration;
        this.frameImage = Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, false);
        this.videoIndex = videoIndex;
    }


    public String getVideoURL(){
        return videoURL;
    }

    public String getVideoName(){
        return videoName;
    }

    public Bitmap getFrameImage(){
        return frameImage;
    }

    public int getVideoIndex(){
        return videoIndex;
    }

    public float getVideoDuration(){
        return duration;
    }


}
