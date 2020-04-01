package com.alkhalildevelopers.videodownloaderdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class DownloadFBVideo {
    public static Context Mcontext;
    public static ProgressDialog progressDialog;
    public static SharedPreferences sharedPreferences;
    public static Boolean fromService;
    static String SessionID, Title;


    public static void Start(final Context context , String url , Boolean service ){

        Mcontext = context;
        fromService = service;

        if (!url.startsWith("http://") && !url.startsWith("https://")){
            url = "http://" + url;
        }
        if (!fromService){
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Generating Download Link");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        if (url.contains("facebook.com")){
            new DownloadFacebookVideo().execute(url);
        }
    }

    private static class DownloadFacebookVideo extends AsyncTask<String,Void, Document>{
        Document dc;

        @Override
        protected Document doInBackground(String... urls) {
            try{
                dc = (Document) Jsoup.connect(urls[0]).get();
            }catch (Exception e){

            }
            return dc;
        }


        @Override
        protected void onPostExecute(Document result) {
            super.onPostExecute(result);
            if (!fromService){
                progressDialog.dismiss();
            }
            try {
                String URL = result.select("meta[property=\"og:video\"]").last().attr("content");
                Title = result.title();
                new DownloadFile().Downloading(Mcontext,URL,Title,".mp4");

            }catch (NullPointerException e){
                e.printStackTrace();
                Toast.makeText(Mcontext, "Something Went Wrong", Toast.LENGTH_SHORT).show();

            }


        }
    }
}
