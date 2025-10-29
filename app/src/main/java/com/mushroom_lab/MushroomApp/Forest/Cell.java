package com.mushroom_lab.MushroomApp.Forest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
//Class for 20x20m cell of forest, contains info about total time spent in this place
// and total number of mushrooms that found here
public class Cell implements Serializable {
    public Map<String, Integer> mushrooms = new HashMap<String, Integer>();
    public int time; public int num_gr; //Total number of all types
    public Cell(int num_gr, int time){
        this.num_gr = num_gr;
        this.time = time;
    }
}
