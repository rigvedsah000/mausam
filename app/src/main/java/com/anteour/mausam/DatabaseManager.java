package com.anteour.mausam;

import android.content.Context;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

//This will be a singleton class since database will be same throughout the app
@Database(entities = {DayTemp.class}, version = 1, exportSchema = false)
abstract class DatabaseManager extends RoomDatabase {

    private static final String DATABASE_NAME = "temperature_database";
    private static DatabaseManager instance = null;

    //Synchronized to get single instance in multi-threaded process
    static synchronized DatabaseManager getInstance(Context c) {
        if (instance == null) {
            instance = Room.databaseBuilder(c,
                    DatabaseManager.class,
                    DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;

    }

    abstract DayTempDao getCityTempDao();
}

//Table in database
@Entity(tableName = "day_temp")
class DayTemp {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private float temp;
    private float temp_max;
    private float temp_min;
    private String timeStamp;
    //Store city name
    private String name;

    DayTemp(float temp, float temp_max, float temp_min, String timeStamp, String name) {
        this.temp = temp;
        this.temp_max = temp_max;
        this.temp_min = temp_min;
        this.timeStamp = timeStamp;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    float getTemp() {
        return temp;
    }

    float getTemp_max() {
        return temp_max;
    }

    float getTemp_min() {
        return temp_min;
    }

    String getTimeStamp() {
        return timeStamp;
    }

    String getName() {
        return name;
    }
}

@Dao
interface DayTempDao {
    /*This query will get all DayTemp Objects in database.
    DayTemp Object contains every temperature (min,max,avg) for a specific day and a specific city.
     */
    @Query("SELECT * FROM day_temp WHERE name = :name")
    List<DayTemp> findByCity(String name);

    //This query will insert all DayTemp objects for a specific city.
    @Insert()
    void insertAllForCity(DayTemp... dayTemps);

    //This query will delete all DayTemp objects for a specific city.
    @Query("DELETE FROM day_temp WHERE name=:name")
    void deleteAllforCity(String name);
}
