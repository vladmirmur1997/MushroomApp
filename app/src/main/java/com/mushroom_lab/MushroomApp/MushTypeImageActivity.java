package com.mushroom_lab.MushroomApp;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.mushroom_lab.MushroomApp.Forest.Filter;
import com.mushroom_lab.MushroomApp.Forest.Forest;
import com.mushroom_lab.MushroomApp.Walk.Walk;

import java.util.HashMap;
import java.util.Map;
public class MushTypeImageActivity extends AppCompatActivity {
    public SparseBooleanArray selected; public Walk walk; public boolean flag_traj;
    public boolean flag = false; public ListView MushList; public CheckBox checkbox;
    public Map<String, Boolean> filter_map = new HashMap<String, Boolean>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_image);
        //get forest
        Bundle arguments = getIntent().getExtras();
        Forest forest = (Forest) arguments.getSerializable(Forest.class.getSimpleName());
        walk = forest.walk_list.get(0);
        //adapter
        MushList = findViewById(R.id.mushroomsList);
        ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_multiple_choice, walk.Mush_types);
        MushList.setAdapter(adapter);
        setChecked(); //в соответствии с фильтром
        MushList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                update_pos();
            }
        });
        //отметка, надо ли показывать траекторию
        flag_traj = forest.traj_flag;
        checkbox = findViewById(R.id.check_traj);
        checkbox.setChecked(forest.traj_flag);

    }
    public void checkTraj(View v){
        flag_traj = checkbox.isChecked();
    }
    public void update_pos(){
        selected = MushList.getCheckedItemPositions();
        for (int i = 0; i < walk.Mush_types.size(); i++) {
            //"type" -> true/false
            filter_map.put(walk.Mush_types.get(i), selected.get(i));
        }
    }
    public void setChecked(){
        for (int i = 0; i<walk.Mush_types.size(); i++){
            //отмечаем в соответствии с фильтром в валк, default all =true
            if (walk.filter_map.get(walk.Mush_types.get(i))) {
                MushList.setItemChecked(i, true);
            }
        }
        filter_map = walk.filter_map;
        //чтобы если ничего не трогать(отметки), все равно возвращало не пустой фильтр
    }
    public void select_all(View v){
        for (int i = 0; i<walk.Mush_types.size(); i++) {
            MushList.setItemChecked(i, flag);
        }
        flag = !flag;
        update_pos();
    }
    public void back(View v){
        Filter f = new Filter(filter_map);
        Intent data = new Intent();
        data.putExtra("filter", f);
        data.putExtra("flag_traj", flag_traj);
        setResult(RESULT_OK, data);
        finish();
    }
}