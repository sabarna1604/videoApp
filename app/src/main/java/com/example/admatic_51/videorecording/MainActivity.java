package com.example.admatic_51.videorecording;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    EditText tripCode;
    Button record, stop , start;
    private Uri fileUri;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    public static MainActivity ActivityContext = null;
    Intent intent;
    Double latitude, longitude;
    protected LocationManager locationManager;
    GPSTracker gpsTracker;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        setContentView(R.layout.activity_main);

        tripCode = (EditText) findViewById(R.id.tripCode);
        record = (Button) findViewById(R.id.record);

        ActivityContext = this;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //File write logic here
        }
//        getLocationFromAddress("Tambaram");
         gpsTracker = new GPSTracker(this);
        if (!gpsTracker.getIsGPSTrackingEnabled()) {
            gpsTracker.showSettingsAlert();
        }

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                String code = tripCode.getText().toString();
                thredFunction(true,code);
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO, code);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
            }
        });

    }


    private static Uri getOutputMediaFileUri(int type, String code) {

        return Uri.fromFile(getOutputMediaFile(type, code));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type, String code) {
        java.util.Date date = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                .format(date.getTime());

        File mediaFile;

        if (type == MEDIA_TYPE_VIDEO) {
            File destination = new File(Environment.getExternalStorageDirectory(),"DriveRecordedVideos");
            destination.mkdir();
            mediaFile =  new File(destination,code + timeStamp + ".mp4");

        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // After camera screen this code will excuted

        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                Toast.makeText(this,  "Video saved to: " +
                        data.getData(), Toast.LENGTH_LONG).show();
                handler.removeCallbacks(null);
                handler.removeMessages(0);

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, "User cancelled the video capture.",
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Video capture failed.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    final Handler handler = new Handler();
  public void thredFunction(final boolean ischecked , final String code){
      final Runnable runnable=new Runnable() {
          java.util.Date date = new java.util.Date();
          String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                  .format(date.getTime());
          public void run() {
              if (ischecked) {
                  gpsTracker.writeToCSVFile(code, timeStamp);
                  handler.postDelayed(this, 1000);
              }
          }
      };
      handler.post(runnable);
  }
}
