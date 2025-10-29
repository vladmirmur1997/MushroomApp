package com.mushroom_lab.MushroomApp;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;

import com.mushroom_lab.MushroomApp.Forest.Filter;
import com.mushroom_lab.MushroomApp.Forest.Forest;
import com.mushroom_lab.MushroomApp.Walk.WalkRemoveDialog.RemoveInterface.RemoveInterface;
import com.mushroom_lab.MushroomApp.Walk.WalkRemoveDialog.WalkRemoveDialog;
import java.util.HashMap;
import java.util.Map;

public class LoadWalkActivity extends AppCompatActivity implements RemoveInterface {
    public SparseBooleanArray selected;
    public ArrayAdapter<String> adapter;
    public float H_p; public float W_p; public ScrollView scrollView;
    public boolean flag = false; public boolean remove_flag = false;
    public ListView walkList; public Forest forest;
    public Map<String, Boolean> filter_map = new HashMap<String, Boolean>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_walk);
        //get forest
        Bundle arguments = getIntent().getExtras();
        forest = (Forest) arguments.getSerializable(Forest.class.getSimpleName());
        //forest list size занимает половину высоты экрана
        scrollView = findViewById(R.id.F_scroll);
        calc_screen_size(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams((int) W_p, (int) H_p/2));
        //adapter
        walkList = findViewById(R.id.walksList);
        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_multiple_choice, forest.all_walks);
        walkList.setAdapter(adapter);
        setChecked(); //в соответствии с фильтром
        walkList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                update_pos();
            }
        });
    }
    void calc_screen_size(Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        H_p = metrics.heightPixels; W_p = metrics.widthPixels;
    }
    public void update_pos(){
        selected = walkList.getCheckedItemPositions();
        for (int i = 0; i < forest.all_walks.size(); i++) {
            //"type" -> true/false
            filter_map.put(forest.all_walks.get(i), selected.get(i));
        }
    }
    public void delete(View v){
        WalkRemoveDialog dialog = new WalkRemoveDialog();
        //Bundle args = new Bundle();
        dialog.show(getSupportFragmentManager(), "custom");
        int N = filter_map.size();
    }
    public void remove()
    {
        int N = forest.all_walks.size();
        String name = forest.all_walks.get(N-1);
        adapter.remove(name);
        adapter.notifyDataSetChanged();
        remove_flag = true;
        //rewrite forest file
    }
    public void setChecked(){
        for (int i = 0; i<forest.all_walks.size(); i++){
            //отмечаем в соответствии с фильтром в валк, default all =true
            if (forest.walk_filter_map.get(forest.all_walks.get(i))) {
                walkList.setItemChecked(i, true);
            }
        }
        filter_map = forest.walk_filter_map;
        //чтобы если ничего не трогать(отметки), все равно возвращало не пустой фильтр
    }
    public void select_all(View v){
        for (int i = 0; i < forest.all_walks.size(); i++) {
            walkList.setItemChecked(i, flag);
        }
        flag = !flag;
        update_pos();
    }
    public void back(View v){
        Filter f = new Filter(filter_map);
        Intent data = new Intent();
        data.putExtra("filter_walks", f);
        data.putExtra("Remove_flag", remove_flag);
        setResult(RESULT_OK, data);
        finish();
    }
}
