package com.example.mricinema3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class VideoImage extends ImageButton{

    private Video video;
    private TextView text;
    public static String URL_KEY = "video_url";
    public static String TITLE_KEY = "video_title";

    public VideoImage(Context context) {
        super(context);
    }

    public VideoImage(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public VideoImage(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    public void setVideo(Video video) {
        this.video = video;
        this.setImageBitmap(video.getFrameImage());
    }

    public void activateClickListener(Context context){
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changeActivityIntent = new Intent(context, VideoPlayerActivity.class);
                changeActivityIntent.putExtra(URL_KEY, video.getVideoURL());
                changeActivityIntent.putExtra(TITLE_KEY, video.getVideoName());
                context.startActivity(changeActivityIntent);
            }
        });
    }

}

