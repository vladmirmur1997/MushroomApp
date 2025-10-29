package com.mushroom_lab.MushroomApp;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import android.location.Location;
import android.os.Looper;
import android.os.PowerManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

public class loc_gms_Service extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback; private Location location;
    public Context context;
    public PowerManager.WakeLock wakelock;
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        int type = 0;
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        NotificationChannel channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            type = ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
            channel = new NotificationChannel("CHANNEL_ID", "Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("channel for foreground service notification");
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification =
                new NotificationCompat.Builder(this, "CHANNEL_ID")
                        .build();
        ServiceCompat.startForeground(this, 1, notification, type);
        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakelock =  powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"motionDetection:keepAwake");
        wakelock.acquire(10*60*6000L /*60 minutes*/);
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateDistanceMeters(0)
                .build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                location = locationResult.getLastLocation();
                double y = location.getLongitude();
                double x = location.getLatitude();
                sendMessageToActivity(x, y);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            //sendMessageToActivity(60,30);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        wakelock.release();
    }
    private void sendMessageToActivity(double x, double y) {
        Intent intent = new Intent("GPS");
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        sendBroadcast(intent);
    }
}
