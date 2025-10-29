package com.mushroom_lab.MushroomApp.Grib;

import java.io.Serializable;

public class Grib implements Serializable {
    public double x;
    public double y;
    public String type;
    public Grib(double x, double y, String type)
    {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
