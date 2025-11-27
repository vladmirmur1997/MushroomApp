package com.mushroom_lab.MushroomApp;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.mushroom_lab.MushroomApp.Forest.Filter;
import com.mushroom_lab.MushroomApp.Forest.Forest;
import com.mushroom_lab.MushroomApp.Walk.WalkRemoveDialog.RemoveInterface.RemoveInterface;
import com.mushroom_lab.MushroomApp.Walk.WalkRemoveDialog.WalkRemoveDialog;
import java.util.HashMap;
import java.util.Map;

public class LoadWalkActivity extends AppCompatActivity implements RemoveInterface {
    public SparseBooleanArray selected;
    SQLiteDatabase db;
    SimpleCursorAdapter userAdapter;
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
        walkList = findViewById(R.id.walksList);
        walkList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                update_pos();
            }
        });
        db = getBaseContext().openOrCreateDatabase("mushs.db", MODE_PRIVATE, null);
        //в соответствии с фильтром:
        upd_adapter();
        setChecked();
    }
    public void upd_adapter() {
        //notfyDataSetChanged not working for cursor adapter
        Cursor query = db.rawQuery("SELECT * FROM walks WHERE forest = '" + forest.num + "';", null);
        String[] headers = new String[] {"walk_name"};
        //userAdapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item,
        //        query, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
        userAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice,
                query, headers, new int[]{android.R.id.text1}, 0);
        walkList.setAdapter(userAdapter);
        //query.close();
    }
    void calc_screen_size(Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        H_p = metrics.heightPixels; W_p = metrics.widthPixels;
    }
    public void update_pos(){
        selected = walkList.getCheckedItemPositions();
        Cursor cur =  db.rawQuery("SELECT * FROM walks WHERE forest = '" + forest.num + "';", null);
        cur.moveToFirst();
        for (int i = 0; i < selected.size(); i++) {
            //"name" -> true/false
            //filter_map.put(forest.all_walks.get(i), selected.get(i));
            String name = cur.getString(3);
            filter_map.put(name, selected.get(i));
            cur.moveToNext();
        }
        cur.close();
    }
    public void delete(View v){
        WalkRemoveDialog dialog = new WalkRemoveDialog();
        //Bundle args = new Bundle();
        dialog.show(getSupportFragmentManager(), "custom");
        int N = filter_map.size();
    }
    public void remove()
    {
        Cursor cur =  db.rawQuery("SELECT * FROM walks WHERE forest = " + forest.num + ";", null);
        cur.moveToLast();
        String name = cur.getString(3);
        db.execSQL("DELETE FROM walks WHERE walk_name = '" + name + "' AND forest = " + forest.num);
        db.execSQL("DELETE FROM mushs WHERE walk_name = '" + name + "' AND forest = " + forest.num);
        db.execSQL("DELETE FROM trajs WHERE walk_name = '" + name + "' AND forest = " + forest.num);
        upd_adapter();
        cur.close();
        filter_map.remove(name);
    }
    public void setChecked(){
        //чтобы если ничего не трогать(отметки), все равно возвращало не пустой фильтр
        Cursor cur =  db.rawQuery("SELECT * FROM walks WHERE forest = '" + forest.num + "';", null);
        cur.moveToFirst();
        for (int i = 0; i < cur.getCount(); i++){
            //отмечаем в соответствии с фильтром в валк
            String name = cur.getString(3);
            if (forest.walk_filter_map.get(name)) {
                walkList.setItemChecked(i, true);
            }
            cur.moveToNext();
        }
        cur.close();
        filter_map = forest.walk_filter_map;
    }
    public void select_all(View v){
        for (int i = 0; i < selected.size(); i++) {            //forest.all_walks.size()
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
