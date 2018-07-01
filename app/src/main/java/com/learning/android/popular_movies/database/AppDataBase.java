package com.learning.android.popular_movies.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.util.Log;

@Database(entities = {Movie.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class, GenreIdsConverter.class})
public abstract class AppDataBase extends RoomDatabase{
    private static final String LOG_TAG = AppDataBase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static  final String DATABASE_NAME = "favoritemovies";
    private static AppDataBase sInstance;

    public static AppDataBase getsInstance(Context context){
        if (sInstance == null){
            synchronized (LOCK){
                Log.d(LOG_TAG, "Creating new database instance.");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                                                 AppDataBase.class,
                                                 AppDataBase.DATABASE_NAME)
                                .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance.");
        return sInstance;
    }

    public abstract MovieDao movieDao();
}
