package com.mushroom_lab.MushroomApp.Forest;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mushroom_lab.MushroomApp.Grib.Grib;
import com.mushroom_lab.MushroomApp.Walk.Walk;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class Forest implements Serializable {
    public double x0; public double y0; //начальная точка отсчета леса
    Double grid_stepsize_y = 0.001D; Double grid_stepsize_x;//linear size of cell
    public int num; public int number_of_walks = 0; //число прогулок, сохр. в файлы!
    public File path; public boolean traj_flag = true;
    //добавить классы тропинка, ориентир, список объектов классов потом в reDraw
    public int max_num = 0; //максимальное число грибов в cell
    public ArrayList<Walk> walk_list = new ArrayList<>(); //loaded walks
    public ArrayList<String> all_walks = new ArrayList<>();
    public Map<String, Boolean> walk_filter_map = new HashMap<String, Boolean>(); //types selection for image
    public Map<Key, Cell> gridmap = new HashMap<Key, Cell>();
        //сетка леса, ключ = массив из 2 элементов
    public Forest(Walk walk, double x, double y, int num, File path, SQLiteDatabase db) {
        walk_list.add(walk); this.num = num; this.path = path;
        this.x0 = x; this.y0 = y;
        double cos_lat = Math.cos(Math.toRadians(x0));
        grid_stepsize_x = grid_stepsize_y*cos_lat;
        get_forest_info();
        read_allwalks();
        //for (int i = 0; i < all_walks.size(); i++ ){
        //    walk_filter_map.put(all_walks.get(i), false);
        //}
        //теперь то же с бд:
        Cursor cur =  db.rawQuery("SELECT * FROM walks WHERE forest = '" + num + "';", null);
        cur.moveToFirst();
        for (int i = 0; i < cur.getCount(); i++ ){
            String name = cur.getString(3);
            walk_filter_map.put(name, false);
            cur.moveToNext();
        }
        cur.close();
    }
    public void get_max(){
        for (Key key : gridmap.keySet()){
            if (gridmap.get(key).num_gr > max_num)
                max_num = gridmap.get(key).num_gr;
        }
    }
    public void addPoint(Double x, Double y) { //update cell time
        Key key = GetKey(x,y);
        addPointCell(key);
    }
    public void addGrib(Double x, Double y, String type) { //update num of mushrooms in cell
        Key key = GetKey(x,y);
        addGribCell(key, type);
    }
    public void addPointCell(Key key){
        if (gridmap.containsKey(key)) {
            gridmap.get(key).time++;
        } else {
            gridmap.put(key, new Cell(0,1));
            //make empty map in cell
            Walk walk = this.walk_list.get(0);
            for (int i =0; i<walk.Mush_types.size(); i++){
                //Map(type, 0)
                gridmap.get(key).mushrooms.put(walk.Mush_types.get(i), 0);
            }
        }
    }
    public void addGribCell(Key key, String type){ //add (or update) in case new mushroom
        if (gridmap.containsKey(key)) {
            gridmap.get(key).num_gr++;
        } else {
            gridmap.put(key, new Cell(1,1));
        }
        if (gridmap.get(key).mushrooms.containsKey(type)){
            int n = gridmap.get(key).mushrooms.get(type);
            gridmap.get(key).mushrooms.put(type, n+1);
        } else {
            gridmap.get(key).mushrooms.put(type, 1);
        }
    }
    public Key GetKey(Double x_, Double y_) {
        Key key = new Key(0,0);
        key.x = Math.round((float)((x_ - x0)/grid_stepsize_x));
        key.y = Math.round((float)((y_ - y0)/grid_stepsize_y));
        return key;
    }
    public double[] GetCell_coords(Key key) {
        double cos_lat = Math.cos(Math.toRadians(x0));
        double x_0 = grid_stepsize_x*(key.x) + x0 - grid_stepsize_x/2;
        double y_0 = grid_stepsize_y*(key.y) + y0 - grid_stepsize_y/2;
        double x_1 = grid_stepsize_x*(key.x) + x0 + grid_stepsize_x/2;
        double y_1 = grid_stepsize_y*(key.y) + y0 + grid_stepsize_y/2;
        //надо учесть косинус широты?
        double[] arr = {x_0, y_0,  x_1, y_1};
        return arr;
    }
    public Float Convert_to_meter(Double x_t, Double x_my) {
        float R_earth = 6371000F;
        //first order rough approx!
        return (float) (R_earth*( Math.toRadians(x_t) -  Math.toRadians(x_my)));
    }
    public void recalcCell(Walk w){   //if walk was loaded
        for (int i = 0; i<w.x_traj.size(); i++){
            Key key = GetKey(w.x_traj.get(i), w.y_traj.get(i));
            addPointCell(key);
        }
        for (Grib gr : w.grib_list){
            Key key = GetKey(gr.x, gr.y);
            addGribCell(key, gr.type);
        }
    }
    public void get_walks(SQLiteDatabase db) {
        //all_walks.size()
        /*for (int i = 0; i < all_walks.size(); i++){
            String name = all_walks.get(i);
            if(walk_filter_map.get(name)){
                Walk walk = new Walk(0,0);
                Get_xy(walk, path, i+1, db);
                walk_list.add(walk);
                recalcCell(walk);
            }
        }*/
        for (String name : walk_filter_map.keySet()){
            if (walk_filter_map.get(name)){
                Walk walk = new Walk(0,0);
                Get_xy(walk, name, db); //N не нужен
                walk_list.add(walk);
                recalcCell(walk);
            }
        }
        //так мы обновляем только грибы, не время
    }
    public void Get_xy(Walk walk, String name, SQLiteDatabase db) {
        //get trajectory
        /*BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    path + "/" + num + "/" + N + "/trajectory.txt"));
            String line = reader.readLine();
            while (line != null) {
                String[] coords = line.split(" ");
                walk.x_traj.add(Double.parseDouble(coords[0]));
                walk.y_traj.add(Double.parseDouble(coords[1]));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //get mushrooms
        try {
            reader = new BufferedReader(new FileReader(
                    path + "/" + num + "/" + N + "/mushrooms.txt"));
            String line = reader.readLine();
            while (line != null) {
                String[] coords = line.split(" ");
                walk.grib_list.add(new Grib(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), coords[2]));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //DataBase, mushs
        Cursor cur =  db.rawQuery("SELECT * FROM mushs WHERE walk_name = '" + name + "';", null);
        cur.moveToFirst();
        do{
            Double x1 = cur.getDouble(4);
            Double y1 = cur.getDouble(5);
            String type = cur.getString(3);
            walk.grib_list.add(new Grib(x1,y1,type));
            // do what ever you want here
        } while(cur.moveToNext());
        cur.close();
        //DataBase, trajs
        cur =  db.rawQuery("SELECT * FROM trajs WHERE walk_name = '" + name + "';", null);
        cur.moveToFirst();
        do{
            Double x1 = cur.getDouble(3);
            Double y1 = cur.getDouble(4);
            walk.x_traj.add(x1); walk.y_traj.add(y1);
            // do what ever you want here
        } while(cur.moveToNext());
        cur.close();

    }
    public void get_forest_info() {
        BufferedReader reader;
        try {
            //try to open forest folder with initial coords file
            reader = new BufferedReader(new FileReader(
                    path + "/" + num + "/Forest_initial_coordinates.txt"));
            String line = reader.readLine();
            reader.close();
            String[] coords = line.split(" ");
            this.x0 = Double.parseDouble(coords[0]); this.y0 = Double.parseDouble(coords[1]);
            //try to read number of walks
            reader = new BufferedReader(new FileReader(
                    path + "/" + num + "/Walks_number.txt"));
            number_of_walks = Integer.valueOf(reader.readLine());
            reader.close();
            //First initialization of new forest:
        } catch (IOException e) {
            //make folder for forest
            File directory = new File(
                    path + "/" + num); //Integer.toString(num));
            directory.mkdirs();
            //make initial coords file
            try (FileWriter coords = new FileWriter(
                    path + "/" + num + "/Forest_initial_coordinates.txt", false)) {
                coords.write(x0 + " " + y0);
                coords.flush();
                //coords.close();
            } catch (IOException ex) {}
        }
        upd_num_walks_file();
    }
    public void upd_num_walks_file(){
        //make num
        try(FileWriter number = new FileWriter(path + "/" + num + "/Walks_number.txt", false)) {
            number.write(Integer.toString(number_of_walks));
            number.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
    public void write_markers(ArrayList<OverlayItem> items){
        //items.get(ind).getPoint().getLongitude()
        try(FileWriter log = new FileWriter(path + "/" + num + "/markers.txt", false)) {
            for (OverlayItem it : items){
                double x = it.getPoint().getLatitude();
                double y = it.getPoint().getLongitude();
                String name = it.getTitle();
                log.write(Double.toString(x) + " " + Double.toString(y) + " " + name + "\n");
            }
            log.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
    public ArrayList<OverlayItem> read_markers(){
        BufferedReader reader;
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        try {
            reader = new BufferedReader(new FileReader(
                    path + "/" + num + "/markers.txt"));
            String line = reader.readLine();
            while (line != null) {
                String[] coords = line.split(" ");
                Double x = Double.parseDouble(coords[0]); Double y = Double.parseDouble(coords[1]);
                String name = coords[2];
                items.add(new OverlayItem(name, "Description",
                        new GeoPoint(x, y)));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }
    public void read_allwalks(){
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    path + "/" + num + "/walk_names.txt"));
            String line = reader.readLine();
            while (line != null) {
                all_walks.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void write_all_walks(){
        try(FileWriter log = new FileWriter(path + "/" + num + "/walk_names.txt", false)) {
            for (String name : all_walks){
                log.write(name + "\n");
            }
            log.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
    public void remove_last_walk(){
        File directory = new File(
                path + "/" + num + "/" + number_of_walks);
        deleteRecursive(directory);
        String name_last = all_walks.get(number_of_walks-1);
        walk_filter_map.remove(name_last);
        all_walks.remove(number_of_walks-1);
        write_all_walks();
        number_of_walks--;
        upd_num_walks_file();
        //если прогулка была подгружена, удаляем из списка
        for (Walk w : walk_list){
            if (w.name == name_last){
                walk_list.remove(w);
            }
        }

    }
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
