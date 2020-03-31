package com.alkhalildevelopers.videodownloaderdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    EditText linkEditText;
    Button downloadButton;
    DownloadManager downloadManager;
    boolean intenetConnect = false;
    private static  boolean sentToSetting = false;
    SharedPreferences permissionStatus;
    private static final int PERMISSION_CALLBACK_CONSTANT = 101;
    private static final int REQUEST_PERMISSION_SETTING = 102;
    public PrefManger pref;
    int Number;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linkEditText = findViewById(R.id.linkEditText);
        downloadButton = findViewById(R.id.downloadBtn);
        pref = new PrefManger(MainActivity.this);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoLink = linkEditText.getText().toString();
                if (videoLink.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Enter Video Link", Toast.LENGTH_SHORT).show();
                }else{
                    downloadVideo(videoLink);
                }
            }
        });

        //check internet Connectivity
        if (isNetworkAvailable()){
            intenetConnect = true;
        }else {
            intenetConnect = false;
        }

        //check storage-write permission
        permissionStatus = this.getSharedPreferences("permissionStatus",MODE_PRIVATE);
        if (!intenetConnect){
            try {
                Snackbar.make(null,"Please Connect to Internet",Snackbar.LENGTH_LONG);
            }catch (Exception e){
                Toast.makeText(this, "Please Connect to Internet", Toast.LENGTH_SHORT).show();
            }

        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                //show the dialog about your needed permissions
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Storage Permission Required");
                alertDialog.setMessage("This app requires Storage Permission to Save Video in your local Storage");
                alertDialog.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
            }else if(permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,false)){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Need Storage Permission");
                alertDialog.setMessage("This app needs storage Permission");
                alertDialog.setPositiveButton("Grnat", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSetting = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",getPackageName(),null);
                        intent.setData(uri);
                        startActivityForResult(intent,REQUEST_PERMISSION_SETTING);
                        Toast.makeText(MainActivity.this, "Goto Permission to Grant Permission to this App", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }else{
                //just request the permission
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_CALLBACK_CONSTANT);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,true);
            editor.apply();
            editor.commit();
        }


    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permission are granted or not granted
            boolean allPermissionGranted = false;
            for (int i =0;i<grantResults.length;i++){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    allPermissionGranted = true;
                }else{
                    allPermissionGranted = false;
                    break;
                }
            }
            if (allPermissionGranted){
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs phone permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(MainActivity.this,"Unable to get Permission",Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING){
            if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                //we got permissions
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onResume() {
        super.onResume();

        if (sentToSetting) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                Toast.makeText(MainActivity.this,"Permissions Granted",Toast.LENGTH_LONG).show();
            }
        }
    }

    //Let create Method for Downloadings
    public void  downloadVideo(String videoLink){
        if (videoLink.contains(".mp4")){
            File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"Downloaded Video");
            directory.mkdir();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoLink));
            Number = pref.getFileName();
            request.allowScanningByMediaScanner();
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            File rootFile = new File(Environment.getExternalStorageDirectory()+File.separator+"Downloaded Videos");
            Uri path = Uri.withAppendedPath(Uri.fromFile(rootFile),"Video"+Number+".mp4");
            request.setDestinationUri(path);
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
            Toast.makeText(this, "Downloading Video"+ Number+".mp4", Toast.LENGTH_SHORT).show();
            Number++;
            pref.setFileName(Number);


        }
    }


    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
