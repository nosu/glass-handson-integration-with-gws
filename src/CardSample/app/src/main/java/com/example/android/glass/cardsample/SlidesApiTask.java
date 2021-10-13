package com.example.android.glass.cardsample;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.model.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class SlidesApiTask extends AsyncTask<Void, Void, List<String>> {
    private Listener listener;
    private GoogleCredentials credentials;
    private String presentationId;
    private final HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public SlidesApiTask(Listener listener, String presentationId, GoogleCredentials credentials) throws GeneralSecurityException, IOException {
        this.listener = listener;
        this.presentationId = presentationId;
        this.credentials = credentials;
    }

    @Override
    protected List<String> doInBackground(Void... args) {
        List<String> thumbnailUrls = new ArrayList<String>();

        try {
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
            Slides slides = new Slides.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                    .setApplicationName("glass-handson")
                    .build();
            Presentation presentation = slides.presentations().get(presentationId).execute();
            List<Page> pages = presentation.getSlides();
            for(int i = 0; i < pages.size(); ++i){
                Page page = pages.get(i);
                Thumbnail thumbnail = slides.presentations()
                        .pages()
                        .getThumbnail(presentationId, page.getObjectId())
                        .setThumbnailPropertiesThumbnailSize("MEDIUM")
                        .execute();
                thumbnailUrls.add(thumbnail.getContentUrl());
            }

            Log.d("SlidesApiTask", "got thumbnailUrls successfully");
            Log.d("SlidesApiTask", thumbnailUrls.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return thumbnailUrls;
    }

    @Override
    protected void onPostExecute(List<String> result) {
        if (listener != null) {
            listener.onSuccess(result);
        }
    }

    interface Listener {
        void onSuccess(List<String> result);
    }
}
