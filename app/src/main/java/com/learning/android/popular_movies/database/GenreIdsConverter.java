package com.learning.android.popular_movies.database;

import android.arch.persistence.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

public class GenreIdsConverter {
    @TypeConverter
    public static List<Integer> toGenreList(String ids){
        if (ids == null || ids.equals("")){
            return new ArrayList<Integer>();
        }
        else{
            String[] genreStrings = ids.split(",");
            List<Integer> genres = new ArrayList<>();
            for (int i = 0; i < genreStrings.length; i++) {
                genres.add(Integer.parseInt(genreStrings[i]));
            }
            return genres;
        }
    }

    @TypeConverter
    public static String toGenreString(List<Integer> genres){
        if (genres == null || genres.size() == 0){
            return "";
        }
        else{
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < genres.size(); ++i) {
                sb.append(genres.get(i));
                if (i < genres.size() - 1){
                    sb.append(",");
                }
            }
            return sb.toString();
        }
    }
}
