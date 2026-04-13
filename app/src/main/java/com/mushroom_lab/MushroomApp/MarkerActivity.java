package com.mushroom_lab.MushroomApp;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import com.mushroom_lab.MushroomApp.Forest.Forest;

public class MarkerActivity extends AppCompatActivity {
    public String name = "";
    public EditText Etext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        //get forest
        Bundle arguments = getIntent().getExtras();
        Forest forest = (Forest) arguments.getSerializable(Forest.class.getSimpleName());
        //листенер
        Etext = findViewById(R.id.markertext);
    }
    public void back(View v){
        Intent data = new Intent();
        name = Etext.getText().toString();
        data.putExtra("marker", name);
        setResult(RESULT_OK, data);
        finish();
    }
}