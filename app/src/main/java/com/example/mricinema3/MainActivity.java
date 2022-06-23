package com.example.mricinema3;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    private ArrayList<Video> videos;
    private final int VIDEO_CAP = 9;
    private String[] admittedExtensions = {"video/mp4", "video/x-msvideo", "video/x-ms-wmv"};
    private final int SELECT_FILES_PERMISSION_CODE = 2;
    private final int REQUEST_READ_PERMISSION_CODE = 3;
    private Button selectVideosButton;
    private final int VIDEOS_PER_ROW = 3;
    private final String FILE_NAME = "video_paths";

    private final String KEY_SAVED_VIDEOS = "saved_parcels";
    private final String KEY_VIDEOS_NAME_ARRAY = "VIDEOS_ARRAY";

    private View layoutView;

    private Intent myIntent;
    private final String FIRST_TIMER = "first_timer";
    private final String PREFS_NAME = "preferences";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, 0);
       if(sharedPrefs.getBoolean(FIRST_TIMER, true)){
           setContentView(R.layout.main_view_layout);
           Toast.makeText(this, "Hola :)", Toast.LENGTH_LONG).show();
           sharedPrefs.edit().putBoolean(FIRST_TIMER, false).commit();
           selectVideosButton = (Button) findViewById(R.id.select_videos_button);
           selectVideosButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   fileSelection();
               }
           });
       }
       else{
           activateLoadingScreen();
           new LoadVideosTask().execute(1);
       }
    }

    public void fileSelection(){
        if (Build.VERSION.SDK_INT >= 26) {
            int result = this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            int result2 = this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            System.out.println(result);
            System.out.println(result2);
            System.out.println(PackageManager.PERMISSION_GRANTED);
            if(result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED){
                System.out.println("Granted");
                selectFiles();
            }
            else{
                try {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_READ_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }




    public void selectFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String type = "video/*";

        intent.setType(type);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        try{
            startActivityForResult(intent, SELECT_FILES_PERMISSION_CODE);
        }catch(Exception e){
            System.out.println(e);
            System.out.println("You may not have videos in your system");
        }
    }

    public void openVideos(Intent data){
        //Seleccionar archivos

                int videoCounter = 0;
                videos = new ArrayList<Video>();

                if(data.getClipData() != null){
                    for(int i = 0; i < data.getClipData().getItemCount(); i++){
                        if(videoCounter < VIDEO_CAP) {
                            Uri videoUri = data.getClipData().getItemAt(i).getUri();
                            //Obtenemos el path del video
                            String path = null;
                            try {
                                path = FilePathExtractor.getPath(this, videoUri);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(this, videoUri);
                            //Duración del video
                            float duration = (float) (Double.parseDouble(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))*Math.pow(10.0, -3));
                            Bitmap videoMap = null;
                            //Thumbnail del video
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                try{
                                    Double microSeconds = Double.parseDouble(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                                    //Sacamos el frame a 1s de empezado el video
                                    Double microSecondsSnap = 1000.0;
                                    Double nFrames = Double.parseDouble(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));
                                    int index = (int) ((nFrames/microSeconds)*microSecondsSnap);

                                    videoMap = retriever.getFrameAtIndex(index);
                                }
                                catch(Exception e){
                                    System.out.println("No se pudo obtener thumbnail del video");
                                }
                                //videoMap = retriever.getPrimaryImage();
                            }
                            System.out.println(path);
                            String[] pathParts = path.split("/");
                            String fileName = pathParts[pathParts.length - 1];

                            Video video = new Video(path, fileName, videoMap, duration, videoCounter);
                            videos.add(video);
                            videoCounter++;
                        }
                    }

                    new LoadVideosTask().execute(1);
                }


    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_FILES_PERMISSION_CODE && resultCode == RESULT_OK && data.getClipData() != null) {
            myIntent = data;
            activateLoadingScreen();
            new OpenVideosTask().execute(1);
        }
        else if(requestCode == REQUEST_READ_PERMISSION_CODE && resultCode == RESULT_OK){
            selectFiles();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_READ_PERMISSION_CODE){
            int nAproved = 0;
            for(int i = 0; i < grantResults.length; i++){
                System.out.println("hey "+grantResults[i]);
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    nAproved = nAproved + 1;
                }
            }
            System.out.println(nAproved);
            System.out.println(grantResults.length);
            if(nAproved == grantResults.length){
                selectFiles();
            }
        }
    }

    private void activateLoadingScreen(){
        setContentView(R.layout.loading_view_layout);
        showLoadingText();
    }

    private void saveVideoURLs(){
        if(videos != null && videos.size()>0) {
            try {
                FileOutputStream fileStream = getApplicationContext().openFileOutput(FILE_NAME, MODE_PRIVATE);
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < videos.size(); i++) {
                    String videoURL = videos.get(i).getVideoURL();
                    stringBuilder.append(videoURL);
                    stringBuilder.append("\n");
                }
                fileStream.write(stringBuilder.toString().getBytes());
                fileStream.close();


            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    private void readVideoURLs(){
        ArrayList<String> paths = new ArrayList<String>();
        try{
            FileInputStream fileStream = getApplicationContext().openFileInput(FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);


            String line = reader.readLine();
            while(line != null){
                paths.add(line);
                line = reader.readLine();
            }

            openVideosWithPaths(paths);
            System.out.println("Here");
            reader.close();
            fileStream.close();
            inputStreamReader.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }


    private void fillThumbnails(){
        View parentView = getLayoutInflater().inflate(R.layout.main_view_layout, null);
        LinearLayout rowsLayout = (LinearLayout) parentView.findViewById(R.id.video_list_rows);
        rowsLayout.removeAllViews();
        if(videos != null && videos.size() != 0){
            int nRows = (int)Math.ceil((double)videos.size()/VIDEOS_PER_ROW);
            for(int i = 0; i < nRows; i++){
                View videoRow = getLayoutInflater().inflate(R.layout.video_row, null);
                if(i != nRows - 1){
                    for(int j = 0; j < VIDEOS_PER_ROW; j++){
                        Video video = videos.get(i*VIDEOS_PER_ROW + j);
                        String idVideo = "video_"+(j+1);
                        int resID = getResources().getIdentifier(idVideo, "id", getPackageName());
                        System.out.println(resID);
                        //System.out.println(getResources().getIdentifier("video_2", "id", getPackageName()));
                        VideoImage videoImage = videoRow.findViewById(resID);
                        videoImage.setVideo(video);
                        videoImage.activateClickListener(this);
                        String idText = "text_"+(j+1);
                        resID = getResources().getIdentifier(idText, "id", getPackageName());
                        TextView textView = videoRow.findViewById(resID);
                        textView.setText(video.getVideoName());

                    }
                    rowsLayout.addView(videoRow);
                }
                else{
                    int excess = videos.size()%VIDEOS_PER_ROW;
                    if(excess == 0){
                        excess = VIDEOS_PER_ROW;
                    }
                    for(int j = 0; j < excess; j++){
                        Video video = videos.get(i*VIDEOS_PER_ROW + j);
                        String idVideo = "video_"+(j+1);
                        int resID = getResources().getIdentifier(idVideo, "id", getPackageName());
                        VideoImage videoImage = videoRow.findViewById(resID);
                        videoImage.setVideo(video);
                        videoImage.activateClickListener(this);
                        String idText = "text_"+(j+1);
                        resID = getResources().getIdentifier(idText, "id", getPackageName());
                        TextView textView = videoRow.findViewById(resID);
                        textView.setText(video.getVideoName());

                    }
                    rowsLayout.addView(videoRow);

                }
            }
        }

        layoutView = parentView;
    }



    private void showLoadingText(){

        Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity,"Cargando videos", Toast.LENGTH_LONG ).show();
            }
        });
    }

    public void openVideosWithPaths(ArrayList<String> paths){
        videos = new ArrayList<Video>();

        for(int i = 0; i < paths.size(); i++){
            String pathVideo = paths.get(i);
            Uri videoUri = Uri.parse(pathVideo);
            //Obtenemos el path del video
            String path = pathVideo;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, videoUri);
            //Duración del video
            float duration = (float) (Double.parseDouble(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))*Math.pow(10.0, -3));
            Bitmap videoMap = null;
            //Thumbnail del video
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                try{
                    Double microSeconds = Double.parseDouble(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    //Sacamos el frame a 1s de empezado el video
                    Double microSecondsSnap = 1000.0;
                    Double nFrames = Double.parseDouble(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));
                    int index = (int) ((nFrames/microSeconds)*microSecondsSnap);

                    videoMap = retriever.getFrameAtIndex(index);
                }
                catch(Exception e){
                    System.out.println("No se pudo obtener thumbnail del video");
                }
                //videoMap = retriever.getPrimaryImage();
            }
            String[] pathParts = path.split("/");
            String fileName = pathParts[pathParts.length - 1];

            Video video = new Video(path, fileName, videoMap, duration, i);
            videos.add(video);


        }

    }

    @Override
    protected void onDestroy() {
        System.out.println("Destroyed");
        super.onDestroy();
        VideoPlayerActivity.mainActivityDestroyed = true;
    }


    public class LoadVideosTask extends AsyncTask<Integer, Integer, Integer> {


        @Override
        protected Integer doInBackground(Integer... integers) {
            saveVideoURLs();
            readVideoURLs();
            fillThumbnails();

            return 1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            setContentView(layoutView);
            selectVideosButton = (Button) findViewById(R.id.select_videos_button);
            selectVideosButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileSelection();
                }
            });
        }
    }

    public class OpenVideosTask extends AsyncTask<Integer, Integer, Integer>{

        @Override
        protected Integer doInBackground(Integer... integers) {
            openVideos(myIntent);

            return 0;

        }
    }
}