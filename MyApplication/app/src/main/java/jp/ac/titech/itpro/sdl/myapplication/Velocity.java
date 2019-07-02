package jp.ac.titech.itpro.sdl.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Velocity extends AppCompatActivity implements SensorEventListener, Runnable{
    private static final String TAG = SensorActivity.class.getSimpleName();
    private final static long GRAPH_REFRESH_PERIOD_MS = 20;

    private static List<Integer> DELAYS = new ArrayList<>();
    static {
        DELAYS.add(SensorManager.SENSOR_DELAY_FASTEST);
        DELAYS.add(SensorManager.SENSOR_DELAY_GAME);
        DELAYS.add(SensorManager.SENSOR_DELAY_UI);
        DELAYS.add(SensorManager.SENSOR_DELAY_NORMAL);
    }

    private static final float ALPHA = 0.75f;


    private TextView timeView;
    private TextView distanceView;
    private TextView velocityView;


    private SensorManager manager;
    private Sensor sensor;

    private final Handler handler = new Handler();
    private final Timer timer = new Timer();

    private float distance=0;
    private float velocity=0;

    private float rdy=0;
    private float rvy=0;
    private float oldrvy=0;
    private float oldray=0;

    private float vdy=0;
    private float vvy=0;
    private float oldvvy=0;
    private float vay=0;
    private float oldvay=0;

    private float firstY=0;

    int shD=1;
    int shV=1;

    int Cal=0;//for calibration

    private float time = 0;
    private int rate;
    private int accuracy;
    private long prevTimestamp;

    private int delay = SensorManager.SENSOR_DELAY_NORMAL;
    private int type = Sensor.TYPE_ACCELEROMETER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_velocity);

        Intent intent = getIntent();
        int num = intent.getIntExtra("Num",0);
        Log.d(TAG, "Num: "+ Integer.toString(num));
        distance=num;

        distanceView = findViewById(R.id.distance_value);
        velocityView = findViewById(R.id.velocity_value);
        timeView = findViewById(R.id.time_value);

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (manager == null) {
            Toast.makeText(this, R.string.toast_no_sensor_manager, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        sensor = manager.getDefaultSensor(type);
        if (sensor == null) {
            String text = getString(R.string.toast_no_sensor_available, sensorTypeName(Sensor.TYPE_ACCELEROMETER));
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            finish();
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Calibration");
                Context context = getApplicationContext();
                CharSequence text = "Calibration";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();
                Cal=1;
                rvy=0;
                vvy=0;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        manager.registerListener(this, sensor, delay);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(Velocity.this);
            }
        }, 0, GRAPH_REFRESH_PERIOD_MS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        timer.cancel();
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long ts = event.timestamp;
        rate = (int) (ts - prevTimestamp) / 1000;

        final float NS2S = 1.0f / 1000000000.0f;
        float dT = (ts - prevTimestamp)*NS2S;
        time+=dT;

        float ray =(int) event.values[1];

        if (prevTimestamp==0||Cal==1){//Fist step or Pushing calibration button
            if(Cal!=1) {
                dT = 0;//initial dT reset
                time = 0;
            }

            //initial state
            firstY=ray;
            Cal=0;
            ray=0;

        }else{//initial state = 0
            ray -= firstY;
        }

        rvy+=ray*dT;
        Log.d(TAG, "(ray+oldray)/2=" + (ray+oldray)/2*dT);
        rvy/=shV;
        oldray= ray;

        rdy+=rvy*dT;
        rdy/=shD;
        oldrvy=rvy;


        vay = ALPHA * vay + (1 - ALPHA) * ray;

        vvy=(oldvay+vay)/2*dT+vvy;
        vvy/=shV;
        oldvay=vay;

        vdy=(vvy+oldvvy)/2*dT+vdy;
        vdy/=shD;
        oldvvy=vvy;

        Log.i(TAG, "Accel=" + ray);
        Log.i(TAG, "Velo=" + rvy );
        Log.i(TAG, "Dist=" + rdy);

        prevTimestamp = ts;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "onAccuracyChanged");
        this.accuracy = accuracy;
    }

    private String sensorTypeName(int sensorType) {
        try {
            Class klass = Sensor.class;
            for (Field field : klass.getFields()) {
                String fieldName = field.getName();
                if (fieldName.startsWith("TYPE_") && field.getInt(klass) == sensorType)
                    return fieldName;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        //Log.d(TAG, "run: "+vvy);
        velocityView.setText(getString(R.string.velocity,rvy, rvy*3.6));
        distanceView.setText(getString(R.string.distance, distance-Math.abs(rdy)));
        timeView.setText(getString(R.string.time, (distance-Math.abs(rdy)) / Math.abs(rvy) / 60, (distance-Math.abs(rdy)) / Math.abs(rvy) % 60));

        if(distance-Math.abs(rdy)<=0){
            Context context = getApplicationContext();
            CharSequence text = "Completed";
            int duration = Toast.LENGTH_LONG;
            Toast.makeText(context, text, duration).show();
            finish();
        }
    }
}
