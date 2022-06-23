package com.example.mricinema3;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private TextView videoTitle;
    private boolean fullScreenOn = false;
    private Button fullScreenButton;
    private float defaultWidthPlayer;
    private float defaultHeightPlayer;
    private float defaultFullScreenButtonX;
    private float defaultFullScreenButtonY;
    public static boolean mainActivityDestroyed;

    protected void onCreate(Bundle savedInstanceState){
        mainActivityDestroyed = false;
        super.onCreate(savedInstanceState);
        System.out.println("Crear");
        setContentView(R.layout.video_player_layout);
        videoView = findViewById(R.id.video_view);
        videoTitle = findViewById(R.id.video_title_player);
        fullScreenButton = findViewById(R.id.full_screen_button);


        Intent intent = getIntent();
        String videoURL = intent.getStringExtra(VideoImage.URL_KEY);
        String videoTitleString = intent.getStringExtra(VideoImage.TITLE_KEY);
        videoTitle.setText(videoTitleString);
        System.out.println(videoURL);
        Uri uri = Uri.parse(videoURL);
        videoView.setVideoURI(uri);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        videoView.start();

        fullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fullScreenOn){
                    activateFullScreen();
                }
                else{
                    restoreView();
                }
            }
        });

        defaultWidthPlayer = videoView.getLayoutParams().width;
        defaultHeightPlayer = videoView.getLayoutParams().height;



        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public void activateFullScreen(){
        System.out.println("Activate full screen");
        hideStatusBar();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) videoView.getLayoutParams();
        layoutParams.width = metrics.widthPixels;
        layoutParams.height = metrics.heightPixels;
        videoView.setLayoutParams(layoutParams);
        fullScreenOn = true;
    }
    public void restoreView(){
        System.out.println("Restoring view");
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) videoView.getLayoutParams();
        layoutParams.width = (int)(defaultWidthPlayer);
        layoutParams.height = (int)(defaultHeightPlayer);
        videoView.setLayoutParams(layoutParams);
        fullScreenOn = false;
        restoreStatusBar();
    }

    @Override
    public void onBackPressed() {
        if(fullScreenOn){
            restoreView();
        }
        else if(!mainActivityDestroyed){
            super.onBackPressed();
        }
        else{
            Context context = getApplicationContext();
            Intent changeActivityIntent = new Intent(context, LaunchActivity.class);
            context.startActivity(changeActivityIntent);
        }
    }


public void hideStatusBar(){
    if (Build.VERSION.SDK_INT < 16) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    } else {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
    }
}

public void restoreStatusBar(){
        if(Build.VERSION.SDK_INT < 16){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        else{
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getActionBar();
            if(actionBar != null){
                actionBar.show();
            }
        }
}


}