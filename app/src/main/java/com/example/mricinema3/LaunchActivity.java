package com.example.mricinema3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LaunchActivity extends AppCompatActivity {

    public int TIME_MILI_SECS = 10000;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_view_layout);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {

                Intent mainIntent = new Intent(LaunchActivity.this,MainActivity.class);
                LaunchActivity.this.startActivity(mainIntent);
                LaunchActivity.this.finish();
                handler.removeCallbacks(this);
            }
        }, TIME_MILI_SECS);

        showLoadingText();
    }


    private void showLoadingText(){

        Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Running");
                Toast.makeText(activity,"Cargando videos", Toast.LENGTH_LONG ).show();
            }
        });
    }

}
