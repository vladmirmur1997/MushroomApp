package com.mushroom_lab.MushroomApp;
import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.mushroom_lab.MushroomApp.Forest.Filter;
import com.mushroom_lab.MushroomApp.Forest.Forest;
import com.mushroom_lab.MushroomApp.Forest.Key;
import android.widget.Toast;
import com.mushroom_lab.MushroomApp.Walk.Me.Me;
import com.mushroom_lab.MushroomApp.Walk.Walk;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;
import java.io.File;
import java.util.ArrayList;
import com.mushroom_lab.MushroomApp.Forest.ItemRemoveDialog.ItemRemoveInterface.ItemRemoveInterface;
import com.mushroom_lab.MushroomApp.Forest.ItemRemoveDialog.ItemRemoveDialog;
import java.util.Calendar;
import java.util.Date;

public class WalkActivity extends AppCompatActivity implements ItemRemoveInterface{
    private MapView map = null; public IMapController myMapController;
    private TextView locationText; private TextView rel_locText;
    private Walk walk; public Forest forest; public myDraw mydraw; public String type = "Белый";
    boolean flag = false; boolean flag_save = true; public int time; int num; //номер леса
    public Me me = new Me(); //тут храним текущие координаты и стрелку навигатора
    DisplayMetrics metrics = new DisplayMetrics(); public File path;
    public LocationManager locationManager; private FusedLocationProviderClient locationClient;
    GroundOverlay myGroundOverlay = new GroundOverlay(); public PowerManager.WakeLock wakelock;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
    public ItemizedIconOverlay<OverlayItem> ForestItemizedIconOverlay;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            me.old_x = me.x; me.old_y = me.y;
            me.x = intent.getDoubleExtra("x", 0);
            me.y = intent.getDoubleExtra("y", 0);
            timer_upd();
            //Toast.makeText(context, "received ", Toast.LENGTH_SHORT).show();
        }
    };
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == Activity.RESULT_OK){
                Intent intent = result.getData();
                //сразу рассматриваем случай вызова для активити отрисовки и случай с выбором найденного гриба
                //boolean массив отмечаемых типов грибов в объекте фильтр
                Filter f_type = (Filter) intent.getSerializableExtra("filter");
                //тип найденного гриба
                String type_chosed = intent.getStringExtra("type");
                //рисовать ли траекторию
                Boolean flag_traj = intent.getBooleanExtra("flag_traj", true);
                //было ли выбрано удалить последний поход
                Boolean remove_flag = intent.getBooleanExtra("Remove_flag", false);
                //какие походы отрисовывать, boolean массив в объекте фильтр
                Filter f_walks = (Filter) intent.getSerializableExtra("filter_walks");
                if (remove_flag){
                    forest.remove_last_walk();
                }
                if (f_walks != null){
                    forest.walk_list.clear();
                    forest.walk_list.add(walk);
                    forest.walk_filter_map = f_walks.filter_map; //какие прогулки подгрузить
                    forest.get_walks();
                }
                if (f_type != null){
                    walk.filter_map = f_type.filter_map; //какие типы грибов рисовать
                    forest.traj_flag = flag_traj;
                    //можно сделать напрямую walk.filtermap, а не в resume, ведь он уже создан
                }
                if (type_chosed != null){
                    type = type_chosed;
                    walk.finding(me.x, me.y, type);
                    forest.addGrib(me.x, me.y, type);
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_walk);
        //get num of forest
        Bundle arguments = getIntent().getExtras();
        num = (int) arguments.get("Forest"); path = getFilesDir();
        walk = new Walk(0,0); //forest = new Forest(walk, 0, 0, num, path);
        //buttons
        locationText = findViewById(R.id.locText); rel_locText = findViewById(R.id.relText);
        //location
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //this.getS...
        //Sleep mode
        Context mContext = getApplicationContext();
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wakelock =  powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"motionDetection:keepAwake");
        wakelock.acquire(10*60*6000L /*60 minutes*/);
        //Map
        initialize_map(mContext);
        Resources resources = rel_locText.getResources();
        mydraw = new myDraw(resources, map);
        //
        calc_size();
        //battery
        checkAndRequestBatteryOptimizationExemption();
        //location
        getCurrentLocation();
        startLocService();
    }
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onResume() {
        super.onResume();
        //иначе виснет после выбора
        map.getOverlays().remove(myGroundOverlay);
        map.getOverlays().add(myGroundOverlay);
        IntentFilter filter = new IntentFilter("GPS");
        registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receiver
        unregisterReceiver(broadcastReceiver);
        Intent intent = new Intent(this, loc_gms_Service.class);
        stopService(intent);
        //wakelock.release();
    }
    public void startLocService(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Intent intent = new Intent(this, loc_gms_Service.class);
        ContextCompat.startForegroundService(this, intent);
    }
    public void initialize_map(Context mContext) {
        Configuration.getInstance().load(mContext, PreferenceManager.getDefaultSharedPreferences(mContext));
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        myMapController = map.getController();
        myMapController.setZoom(16);
        map.setMultiTouchControls(true);
    }
    public void calc_size(){
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float W_p = metrics.widthPixels;
        map.setLayoutParams(new ViewGroup.LayoutParams((int) W_p, (int) (W_p*1.4)));
        me.x_arrow = W_p - 50; me.y_arrow = W_p*1.4F-50;
    }
    public void find(View v) {    //Find Button
        Intent intent = new Intent(this, MushTypeChooseActivity.class);
        intent.putExtra(Forest.class.getSimpleName(), forest);
        mStartForResult.launch(intent);
    }
    public void call_gridmap(View v) {   //Statistic Button
        flag = !flag;
    }
    public void set_type(View v) {   //Type Button
        Intent intent = new Intent(this, MushTypeImageActivity.class);
        intent.putExtra(Forest.class.getSimpleName(), forest);
        mStartForResult.launch(intent);
    }
    public void save_walk(View v){  //Save Button
        //current walk has zero number
        //we save it with number_of_walks + 1
        Date currentTime = Calendar.getInstance().getTime();
        //чтобы одну прогулку не сохранить дважды, а перезаписывать,
        if (flag_save) {
            forest.number_of_walks++;
            flag_save = false;
            walk.name = currentTime.toString();
            forest.all_walks.add(currentTime.toString());
        }
        forest.upd_num_walks_file();
        walk.SaveWalk(path, forest.num, forest.number_of_walks);
        forest.walk_filter_map.put(currentTime.toString(), false);
        Toast toast = Toast.makeText(this,
                "saved with N= " + forest.number_of_walks, Toast.LENGTH_LONG);
        toast.show();
        forest.write_all_walks();
    }
    public void load_walk(View v){ //LoadBtn
        //forest.get_walk(); previous walk
        Intent intent = new Intent(this, LoadWalkActivity.class);
        intent.putExtra(Forest.class.getSimpleName(), forest);
        mStartForResult.launch(intent);
    }
    public void add_marker(View v){
        items.add(new OverlayItem("Forest", "Description",
                new GeoPoint(me.x, me.y)));
        update_overlay();
        forest.write_markers(items);
    }
    public void update_overlay(){
        map.getOverlays().remove(ForestItemizedIconOverlay);
        ForestItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(
                this, items, myOnItemGestureListener);
        map.getOverlays().add(ForestItemizedIconOverlay);
        //чтобы не зависало:
        map.getOverlays().remove(myGroundOverlay);
        map.getOverlays().add(myGroundOverlay);
    }
    public class GroundOverlay extends Overlay {
        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (time > 5) {
                mydraw.reDraw(forest, walk, flag, canvas);
                mydraw.drawArrow(me, canvas);
            }
            mydraw.draw_me(me, canvas);
            map.invalidate();
        }
        @Override
        public boolean onDoubleTap(final MotionEvent e, final MapView mapView){
            GeoPoint locGeoPoint = new GeoPoint(me.x, me.y);
            myMapController.setCenter(locGeoPoint);
            return false;
        }
    }
    public void timer_upd(){
        if (time == 0) {
            forest = new Forest(walk, me.x, me.y, num, path);
            map.getOverlays().add(myGroundOverlay);
            items = forest.read_markers();
            ForestItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(
                    this, items, myOnItemGestureListener);
            map.getOverlays().add(ForestItemizedIconOverlay);
            GeoPoint locGeoPoint = new GeoPoint(me.x, me.y);
            myMapController.setCenter(locGeoPoint);
        } else {
            walk.x_traj.add(me.x); walk.y_traj.add(me.y);
            forest.addPoint(me.x, me.y);
            //rotate Arrow
            me.calc_rot_Arrow(walk);
            locationText.setText("Latitude: " + me.x + "\nLongitude: " + me.y);
            /**
            Key key = forest.GetKey(me.x, me.y);
            int t = forest.gridmap.get(key).time;
            rel_locText.setText("size " + walk.x_traj.size());
            //locationText.setText("фильтр: " + forest.walk_list.get(0).filter_map.get("Белый"));*/
        }
        time++;
    }
    OnItemGestureListener<OverlayItem> myOnItemGestureListener
            = new OnItemGestureListener<OverlayItem>(){
        @Override
        public boolean onItemLongPress(int arg0, OverlayItem arg1) {
            // TODO Auto-generated method stub
            ItemRemoveDialog dialog = new ItemRemoveDialog();
            Bundle args = new Bundle();
            args.putInt("orient", arg0);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "custom");
            return false;
        }
        @Override
        public boolean onItemSingleTapUp(int index, OverlayItem item) {
            //IGeoPoint locGeoPoint = item.getPoint(); double x = locGeoPoint.getLatitude();
            rel_locText.setText(item.getTitle());
            return true;
        }
    };
    public void removeItem(int ind){
        //map.getOverlays().remove(ForestItemizedIconOverlay);
        items.remove(ind);
        update_overlay();
        Toast toast = Toast.makeText(this,
                "метка удалена", Toast.LENGTH_LONG);
        toast.show();
        forest.write_markers(items);
    }
    // Function to get the current location (and request permission)
    public void getCurrentLocation() {
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            locationText.setText("not granted");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        // Fetch the last known location
        locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Get latitude and longitude
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    // Display location in TextView
                    locationText.setText("Latitude: " + lat + "\nLongitude: " + lon);
                } else {
                    // Display error message if location is null
                    locationText.setText("Unable to get location");
                }
            }
        });
    }
    private void checkAndRequestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            String packageName = getPackageName();

            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    // Handle exception if the intent cannot be started
                    Toast.makeText(this, "Please manually disable battery optimization for this app in settings.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // If permission is granted, fetch location
            startLocService();
        } else {
            // If permission is denied, show message
            locationText.setText("Location permission denied");
        }
    }
}
