package com.mushroom_lab.MushroomApp;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;
import com.mushroom_lab.MushroomApp.Forest.ForestRemoveDialog.ForestRemoveDialog;
import com.mushroom_lab.MushroomApp.Forest.ForestRemoveDialog.RemoveInterface.RemoveInterface;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RemoveInterface {
    private final static String TAG = "MainActivity";
    public Context context; public float H_p; public float W_p;
    String selectedForest;
    ScrollView scrollView; ListView ForList; int ID = 0 ;
    SQLiteDatabase db; Cursor userCursor; SimpleCursorAdapter userAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //forest list size занимает половину высоты экрана
        scrollView = findViewById(R.id.F_scroll);
        calc_screen_size(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams((int) W_p, (int) H_p/2));
        //читаем список лесов

        ForList = findViewById(R.id.ForestList);
        ForList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                ID = (int)id;
                Cursor query = db.rawQuery("SELECT * FROM forests WHERE _id = " + id + ";", null);
                query.moveToFirst();
                selectedForest = query.getString(1);
                query.close();
                Toast toast = Toast.makeText(v.getContext(),
                        "выбран лес " + selectedForest, Toast.LENGTH_LONG);
                toast.show();
            }
        });

        db = getBaseContext().openOrCreateDatabase("mushs.db", MODE_PRIVATE, null);
        //создаем все таблицы
        //db.execSQL("DROP TABLE IF EXISTS walks");
        //db.execSQL("DROP TABLE IF EXISTS mushs");
        //db.execSQL("DROP TABLE IF EXISTS trajs");
        //db.execSQL("DROP TABLE IF EXISTS forests");
        db.execSQL("CREATE TABLE IF NOT EXISTS forests (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS mushs (_id INTEGER PRIMARY KEY AUTOINCREMENT, forest INTEGER, walk_name TEXT, type TEXT, x REAL, y REAL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS trajs (_id INTEGER PRIMARY KEY AUTOINCREMENT, forest INTEGER, walk_name TEXT, x REAL, y REAL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS walks (_id INTEGER PRIMARY KEY AUTOINCREMENT, forest INTEGER, walk_num INTEGER, walk_name TEXT)");
        //добавляем лес по умолчанию
        Cursor query = db.rawQuery("SELECT * FROM forests;", null);
        if (query.getCount() ==0 ){db.execSQL("INSERT INTO forests (name) VALUES ('Les1')");}
        upd_adapter(); //query.close();
    }
    void calc_screen_size(Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        H_p = metrics.heightPixels; W_p = metrics.widthPixels;
    }
    public void start(View v) {
        Intent intent = new Intent(this, WalkActivity.class);
        //intent.putExtra("Forest", num);
        intent.putExtra("Forest", ID);
        startActivity(intent);
    }
    public void upd_adapter() {
        //notfyDataSetChanged not working for cursor adapter
        Cursor query = db.rawQuery("SELECT * FROM forests;", null);
        String[] headers = new String[] {"_id", "name"};
        ListView ForList = findViewById(R.id.ForestList);
        userAdapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item,
                query, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
        ForList.setAdapter(userAdapter);
        //query.close();
    }
    public void new_forest(View v) {
        EditText ForestText = findViewById(R.id.foresttext);
        String forestName = ForestText.getText().toString();
        if(!forestName.isEmpty()){
            //Db
            //db.execSQL("INSERT INTO forests (name) VALUES ('Les2')");
            db.execSQL("INSERT INTO forests (name) VALUES ('" + forestName + "')");
            upd_adapter();
        }
    }
    public void delete_forest(View v) {
        //String selectedForest = adapter.getItem(num);
        if (selectedForest != null){
            ForestRemoveDialog dialog = new ForestRemoveDialog();
            Bundle args = new Bundle();
            args.putString("Forest", selectedForest);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "custom");
        } else{
            Toast toast = Toast.makeText(this,
                    "Вначале нажмите на нужный лес", Toast.LENGTH_LONG);
            toast.show();
        }
    }
    public void remove(String name) {
        //rewrite forest file
        if (ID != 0) {
            db.execSQL("DELETE FROM forests WHERE _id = " + ID);
            //delete all walks, trajs, mushs
            db.execSQL("DELETE FROM walks WHERE forest = " + ID);
            db.execSQL("DELETE FROM mushs WHERE forest = " + ID);
            db.execSQL("DELETE FROM trajs WHERE forest = " + ID);
        }
        upd_adapter();

    }
    public void onDestroy(){
        super.onDestroy();
        // Закрываем подключение и курсор
        db.close();
        userCursor.close();
    }
}