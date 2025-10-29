package com.mushroom_lab.MushroomApp;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.mushroom_lab.MushroomApp.Forest.Cell;
import com.mushroom_lab.MushroomApp.Forest.Forest;
import com.mushroom_lab.MushroomApp.Forest.Key;
import com.mushroom_lab.MushroomApp.Grib.Grib;
import com.mushroom_lab.MushroomApp.Walk.Walk;
import com.mushroom_lab.MushroomApp.Walk.Me.Me;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import java.util.HashMap;
import java.util.Map;
public class myDraw {
    Resources resources; public int backColor; public int trajColor; public int mushColor;
    public int whiteColor; public int redmushColor; public int pdbrzColor;
    public int orColor; public int lColor; public MapView map;
    public float scale = 2; //(1m = 2px) соотношение пикселей к метрам по умолчанию
    public Map<String, Integer> color_map = new HashMap<String, Integer>();
    public myDraw(Resources resources, MapView map){
        this.resources = resources; this.map = map;
        backColor = resources.getColor(R.color.BackColor,  null);
        trajColor = resources.getColor(R.color.TrajColor,  null);
        mushColor = resources.getColor(R.color.MushColor,  null);
        whiteColor = resources.getColor(R.color.WhiteMushColor,  null);
        redmushColor = resources.getColor(R.color.RedMushColor,  null);
        pdbrzColor = resources.getColor(R.color.PdbrzColor,  null);
        orColor = resources.getColor(R.color.OrColor,  null);
        lColor = resources.getColor(R.color.LColor,  null);
        color_map.put("Белый", whiteColor);
        color_map.put("Подберезовик", pdbrzColor);
        color_map.put("Подосиновик", redmushColor);
        color_map.put("Лисичка", lColor);
        color_map.put("Рыжик", orColor);
        color_map.put("Другое", mushColor);
    }
    public Point recalc_coords(Double x, Double y) {
        GeoPoint locGeoPoint = new GeoPoint(x, y);
        Point out = new Point();
        map.getProjection().toPixels(locGeoPoint, out);
        return out;
    }
    public void Draw_Grib(int x, int y, Integer Rd, int col, Canvas mCanvas) {
        Paint paint = new Paint();
        paint.setColor(col);
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.FILL);
        mCanvas.drawCircle(x, y, Rd, paint);
    }
    public void Draw_Traj_Point(int x, int y, Integer Rd, Canvas mCanvas) {
        Paint paint = new Paint();
        paint.setColor(trajColor);
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.FILL);
        mCanvas.drawCircle(x, y, Rd, paint);
    }
    public void drawCell(Point n1, Point n2, int color, Canvas mCanvas) {
        Paint paint = new Paint();
        paint.setARGB(255, color,255,color);
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.FILL);
        mCanvas.drawRect(n1.x, n1.y, n2.x, n2.y, paint);
    }
    public void drawArrow(Me me, Canvas mCanvas) {
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStrokeWidth(5f);
        for (int i = 0; i < me.arrow.rot_arrow.length - 1; i++) {
            float x0 = me.arrow.rot_arrow[i][0];
            float x1 = me.arrow.rot_arrow[i+1][0];
            float y0 = me.arrow.rot_arrow[i][1];
            float y1 = me.arrow.rot_arrow[i+1][1];
            mCanvas.drawLine(x0, y0, x1, y1, paint);
        }
    }
    public int get_color(int num, int time, Forest forest) {
        int color = 255;
        float I = (float) num / (float) forest.max_num;
        if (forest.max_num != 0) {color = (int) (255*(1 - I));}
        return color;
        //color = num/(time+60) / max(num/(time+60))
        //для больших квадратов - учитывается проведенное время
    }
    public void draw_walk_traj(Walk walk, Canvas mCanvas) {
        //Trajectory Draw, Blue transparent
        for (int i = 0; i < walk.x_traj.size(); i++) {
            Point out = recalc_coords(walk.x_traj.get(i), walk.y_traj.get(i));
            Draw_Traj_Point(out.x, out.y, 6, mCanvas);
        }
    }
    public void draw_walk_mush(Forest forest, Walk walk, Canvas mCanvas){
        //Mushrooms Draw, using grib.type
        for (Grib gr : walk.grib_list){
            //filter map хранится в первой прогулке! //walk.filter_map.get(gr.type)
            boolean F = forest.walk_list.get(0).filter_map.get(gr.type);
            if (F){
                Point out = recalc_coords(gr.x, gr.y);
                int mColor = color_map.get(gr.type);
                Draw_Grib(out.x, out.y, 4, mColor, mCanvas);
            }
        }
    }
    public void draw_me(Me me, Canvas mCanvas) {
        Point out = recalc_coords(me.x, me.y);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.FILL);
        mCanvas.drawCircle(out.x, out.y, 4, paint);
    }
    public void reDraw(Forest forest, Walk walk, boolean flag, Canvas mCanvas) {
        //draw cells
        forest.get_max();
        if (flag) {
            mCanvas.drawColor(Color.WHITE);
            for (Key key: forest.gridmap.keySet()) {
                //int num_grib = forest.gridmap.get(key).num_gr; //all types
                int num_grib = calc_num_grib_of_selected_types(forest, walk, key);
                int time = forest.gridmap.get(key).time;
                //get coords of rectangle(key)
                double[] arr = forest.GetCell_coords(key);
                int color = get_color(num_grib, time, forest);
                Point cell_corner1 = recalc_coords(arr[0], arr[1]);
                Point cell_corner2 = recalc_coords(arr[2], arr[3]);
                drawCell(cell_corner1,  cell_corner2, color, mCanvas);
            }
        }
        //draw walks, mushrooms after trajectory (чтобы не закрашивались)
        if (forest.traj_flag) {
            for (Walk w : forest.walk_list) {
                draw_walk_traj(w, mCanvas);
            }
        }
        for (Walk w: forest.walk_list) {
            draw_walk_mush(forest, w, mCanvas);
        }
        //draw center point
    }
    public Integer calc_num_grib_of_selected_types(Forest forest, Walk walk, Key key){
        int number = 0;
        Cell cell = forest.gridmap.get(key);
        for (int i = 0; i < walk.Mush_types.size(); i++){
            String type = walk.Mush_types.get(i);
            if (walk.filter_map.get(type)){
                //plus number of mushrooms in cell
                number = number + cell.mushrooms.get(type);
            }
        }
        return number;
    }

}
