package com.example.android.popular_movies.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.android.popular_movies.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.security.auth.login.LoginException;

public class NetworkMovieUtils {
    private final static String movieAPiUrlPrefix = "https://api.themoviedb.org/3/movie/";



    private static URL buildURL(String apiKey, boolean sortByPopularity){
        Uri uri = Uri.parse(movieAPiUrlPrefix).buildUpon()
                .appendPath(sortByPopularity ? "popular" : "top_rated")
                .appendQueryParameter("api_key", apiKey)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        }
        catch (MalformedURLException e){
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromURL(String apiKey, boolean sortByPopular){
        String result = null;
        URL url = buildURL(apiKey, sortByPopular);
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");
            if (urlConnection.getResponseCode() != 200){
                Log.e("HTTP Error", "getResponseFromURL error code: " + urlConnection.getResponseCode());
            }
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String partialResult;
            while ((partialResult = bufferedReader.readLine()) != null){
                sb.append(partialResult);
            }
            urlConnection.disconnect();
            result = sb.toString();
        }
        catch (MalformedURLException e){
            Log.e("Malformed URL Error", "getResponseFromURL: "+e.getMessage() );
        }
        catch (IOException e){
            Log.e("IO Error", "getResponseFromURL: "+e.getMessage() );
        }
        return result;
    }
}
