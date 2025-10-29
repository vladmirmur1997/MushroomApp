package com.mushroom_lab.MushroomApp.Forest;
import java.io.Serializable;
import java.util.Map;
public class Filter implements Serializable {
    public Map<String, Boolean> filter_map;
    public Filter(Map<String, Boolean> filter_map){
        this.filter_map = filter_map;
    }
}
