package com.mushroom_lab.MushroomApp.Walk.Me;
import com.mushroom_lab.MushroomApp.Walk.Walk;

import java.io.Serializable;
import java.lang.Math;
public class Me implements Serializable {
    public class Arrow{
        public float[][] dots = {{0, 0}, {-5, 10}, {15, 0}, {-5, -10}, {0, 0}}; //arrow
        public float[][] rot_arrow;
    }
    public Arrow arrow = new Arrow();
    public double x, y;
    public double old_x, old_y; public float x_arrow, y_arrow;
    public float cos_x = 0; public float sin_x = 0; //rotation matrix components
    public void calc_rot_Arrow(Walk walk){
        //arrow
        //move vector, поменял местами+знак минус для OSM
        float dy = - walk.Convert_to_meter(x, old_x);
        float dx = walk.Convert_to_meter(y, old_y);
        float abs = (float) Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2));
        if (abs > 0.01) {
            cos_x = dx / abs;
            sin_x = dy / abs;
        }
        arrow.rot_arrow = rotate(arrow.dots);
    }
    public float[][] rotate(float[][] coords) {
        float[][] arr = new float[coords.length][coords[0].length];
        //rot Matrix apply + translate
        for (int i = 0; i < coords.length; i++) {
            arr[i][0] = coords[i][0] * cos_x - coords[i][1] * sin_x + x_arrow;
            arr[i][1] = coords[i][0] * sin_x + coords[i][1] * cos_x + y_arrow;
        }
        return arr;
    }
}
