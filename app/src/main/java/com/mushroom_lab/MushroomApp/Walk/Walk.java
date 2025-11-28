package com.mushroom_lab.MushroomApp.Walk;
import android.database.sqlite.SQLiteDatabase;

import com.mushroom_lab.MushroomApp.Grib.Grib;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Walk implements Serializable{
    public float x; public float y; public String name;
    public ArrayList<String> Mush_types = new ArrayList<>();
    public Map<String, Boolean> filter_map = new HashMap<String, Boolean>(); //types selection for image
    public ArrayList<Double> x_traj = new ArrayList<>(); public ArrayList<Double> y_traj = new ArrayList<>();
    public ArrayList<Grib> grib_list = new ArrayList<Grib>();
    public Walk(float x, float y) {
        this.x = x; this.y = y;
        Collections.addAll(Mush_types, "Белый", "Подберезовик", "Подосиновик", "Лисичка", "Рыжик", "Другое");
        for (int i = 0; i < Mush_types.size(); i++ ){
            //default imaging og all mushroom types:
            filter_map.put(Mush_types.get(i), true);
        }
    }
    public void finding(double grib_coord_x, double grib_coord_y, String grib_type) {
        Grib grib = new Grib(grib_coord_x, grib_coord_y, grib_type);
        grib_list.add(grib);
    }
    public Float Convert_to_meter(Double x_t, Double x_my) {
        float R_earth = 6371000F;
        //first order rough approx!
        return (float) (R_earth*(Math.toRadians(x_t) -  Math.toRadians(x_my)));
    }
    public void saveDb(SQLiteDatabase db, int numForest, String name, boolean flag_save) {
        for (Grib gr: grib_list) {
            //db.execSQL("INSERT INTO mushs (forest, walk_name, type, x, y) VALUES (0, 0, 'white', 0, 0)");
            db.execSQL("INSERT INTO mushs (forest, walk_name, type, x, y) " +
                    "VALUES (" + numForest + ", '" + name + "', '" + gr.type + "', " +
                    gr.x + ", " + gr.y + ")");
        }
        for (int i=0; i < x_traj.size(); i++) {
            //db.execSQL("INSERT INTO trajs (forest, walk, x, y) VALUES (0, 0, 0, 0)");
            db.execSQL("INSERT INTO trajs (forest, walk_name, x, y) " +
                    "VALUES (" + numForest + ", '" + name + "', " + x_traj.get(i) + ", " +
                    y_traj.get(i) + ")");
        }
    }
}
