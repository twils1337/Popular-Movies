package com.learning.android.popular_movies.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp){
        return new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimStamp(Date date){
        return date == null ? 0 : date.getTime();
    }
}
