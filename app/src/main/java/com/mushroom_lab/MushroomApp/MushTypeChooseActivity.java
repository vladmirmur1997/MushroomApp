package com.mushroom_lab.MushroomApp;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.mushroom_lab.MushroomApp.Forest.Forest;
import com.mushroom_lab.MushroomApp.Walk.Walk;

public class MushTypeChooseActivity extends AppCompatActivity {
    public Walk walk; public String type = "Белый";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_choose);
        //get forest
        Bundle arguments = getIntent().getExtras();
        Forest forest = (Forest) arguments.getSerializable(Forest.class.getSimpleName());
        walk = forest.walk_list.get(0);
        //adapter
        ListView MushList = findViewById(R.id.mushroomsList);
        ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, walk.Mush_types);
        MushList.setAdapter(adapter);
        MushList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                 type = walk.Mush_types.get(position);
            }
        });
    }
    public void back(View v){
        Intent data = new Intent();
        data.putExtra("type", type);
        setResult(RESULT_OK, data);
        finish();
    }
}