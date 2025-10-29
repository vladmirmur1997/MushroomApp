package com.mushroom_lab.MushroomApp;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    int num = 0; ArrayList<String> forests = new ArrayList<String>();
    ArrayAdapter<String> adapter; ListView forest_list_view; String selectedForest;
    ScrollView scrollView;
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
        forests.add("Лес0"); //в списке лесов в файле не сохраняется
        get_forest_list();
        forest_list_view = findViewById(R.id.ForestList);
        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, forests);
        //устанавливаем для списка адаптер
        forest_list_view.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        forest_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                num = position;
                selectedForest = adapter.getItem(position);
                Toast toast = Toast.makeText(v.getContext(),
                        "выбран лес " + forests.get(num) + ", номер: " + num, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
    void calc_screen_size(Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        H_p = metrics.heightPixels; W_p = metrics.widthPixels;
    }
    public void start(View v) {
        Intent intent = new Intent(this, WalkActivity.class);
        intent.putExtra("Forest", num);
        startActivity(intent);
    }
    public void new_forest(View v) {
        EditText ForestText = findViewById(R.id.foresttext);
        String forestName = ForestText.getText().toString();
        if(!forestName.isEmpty()){
            adapter.add(forestName);
            ForestText.setText("");
            adapter.notifyDataSetChanged();
            write_forest_list(forestName+"\n", true);
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
        //adapter.remove(selectedForest);
    }
    public void get_forest_list() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    getFilesDir() + "/forest_list.txt"));
            String line = reader.readLine();
            while (line != null) {
                forests.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void write_forest_list(String name, boolean append) {
        try(FileWriter f_list = new FileWriter(getFilesDir()+"/forest_list.txt", append)) {
            f_list.write(name);
            f_list.flush();
        }
        catch(IOException ex){
            Toast toast = Toast.makeText(this,
                    ex.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
        Toast toast = Toast.makeText(this,
                getFilesDir() + "/forest_list.txt", Toast.LENGTH_LONG);
        toast.show();
    }
    public void remove(String name) {
        adapter.remove(name);
        adapter.notifyDataSetChanged();
        //rewrite forest file
        write_forest_list("", false);
        for (int i = 0; i < forests.size(); i++){
            write_forest_list(forests.get(i) + "\n", true);
        }

    }
}