package com.alkhalildevelopers.videodownloaderdemo;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class DownloadFile {
    public static DownloadManager downloadManager;
    public static long downloadID;
    private static String mBaseFolderPath;

    public static void Downloading(Context context, String url, String title,String ext){
        String cutTitle =  title;
        String characterFilter ="[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";
        cutTitle = cutTitle.replaceAll(characterFilter,"");
        cutTitle= cutTitle.replaceAll("['+.^;,#\"]","");
        cutTitle= cutTitle.replace(" ","-").replace("!","").replace(":","")+ext;
       if (cutTitle.length()> 100){
           cutTitle = cutTitle.substring(0,100) + ext;
       }
       downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
       DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
       request.setTitle(title);
       request.setDescription("Downloading");
       request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
       String folderName = "Facebook Videos";
        SharedPreferences preferences = context.getSharedPreferences("DownloadPath",Context.MODE_PRIVATE);
        if (!preferences.getString("path","Default").equals("Default")){
            mBaseFolderPath = preferences.getString("path","Default");
        }else {
            mBaseFolderPath = android.os.Environment.getExternalStorageDirectory()+ File.separator+folderName;
        }
        String[] bits = mBaseFolderPath.split("/");
        String Dir = bits[bits.length -1];

        request.setDestinationInExternalPublicDir(Dir,cutTitle);
        request.allowScanningByMediaScanner();
//        downloadID = downloadManager.enqueue(request);
        downloadManager.enqueue(request);
        Log.e( "downloadFileName",cutTitle );
        Toast.makeText(context, "Downloaing Start !", Toast.LENGTH_SHORT).show();
    }
}
