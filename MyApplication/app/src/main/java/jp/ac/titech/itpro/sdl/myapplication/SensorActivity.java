package jp.ac.titech.itpro.sdl.myapplication;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SensorActivity extends AppCompatActivity implements SensorEventListener, Runnable {
    private final static String TAG = SensorActivity.class.getSimpleName();
    private final static long GRAPH_REFRESH_PERIOD_MS = 20;

    private static List<Integer> DELAYS = new ArrayList<>();
    static {
        DELAYS.add(SensorManager.SENSOR_DELAY_FASTEST);
        DELAYS.add(SensorManager.SENSOR_DELAY_GAME);
        DELAYS.add(SensorManager.SENSOR_DELAY_UI);
        DELAYS.add(SensorManager.SENSOR_DELAY_NORMAL);
    }

    private static final float ALPHA = 0.75f;

    private TextView typeView;
    private TextView infoView;
    private GraphView xView, vView, aView;
    private TextView DinfoView,VinfoView,AinfoView;



    private SensorManager manager;
    private Sensor sensor;

    private final Handler handler = new Handler();
    private final Timer timer = new Timer();


    private float rdx=0, rdy=0, rdz=0, rD;
    private float rvx=0, rvy=0, rvz=0, rV;
    private float oldrvx=0,oldrvy=0, oldrvz=0;
    private float rA;
    private float oldrax, oldray, oldraz;

    private float vdx=0, vdy=0, vdz=0, vD;
    private float vvx=0, vvy=0, vvz=0, vV;
    private float oldvvx=0,oldvvy=0, oldvvz=0;
    private float vax, vay, vaz, vA;
    private float oldvax=0,oldvay=0, oldvaz=0;

    int shD=1;
    int shV=1;

    private float time = 0;
    private int rate;
    private int accuracy;
    private long prevTimestamp;

    private int delay = SensorManager.SENSOR_DELAY_NORMAL;
    private int type = Sensor.TYPE_ACCELEROMETER;

    private int GravF;
    private float Grav;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_sensor);

        Intent intent = getIntent();
        int num = intent.getIntExtra("Num",0);
        Log.d(TAG, "Num: "+ Integer.toString(num));


        typeView = findViewById(R.id.type_view);
        infoView = findViewById(R.id.info_view);
        xView = findViewById(R.id.d_view);
        DinfoView = findViewById(R.id.Dinfo_view);
        vView = findViewById(R.id.v_view);
        VinfoView = findViewById(R.id.Vinfo_view);
        aView = findViewById(R.id.a_view);
        AinfoView = findViewById(R.id.Ainfo_view);

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (manager == null) {
            Toast.makeText(this, R.string.toast_no_sensor_manager, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        sensor = manager.getDefaultSensor(type);
        if (sensor == null) {
            String text = getString(R.string.toast_no_sensor_available, sensorTypeName(type));
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        manager.registerListener(this, sensor, delay);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(SensorActivity.this);
            }
        }, 0, GRAPH_REFRESH_PERIOD_MS);
    }

    @Override
    public void run() {
        infoView.setText(getString(R.string.info_format, accuracy, rate, time));
        xView.addData(rD, vD);
        DinfoView.setText(getString(R.string.Dinfo_format, vD*shD));
        vView.addData(rV, vV);
        VinfoView.setText(getString(R.string.Vinfo_format, vV*shV));
        aView.addData(rA, vA);
        AinfoView.setText(getString(R.string.Ainfo_format, vA));
    }

    @Override

    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        timer.cancel();
        manager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        int title = 0;
        switch (delay) {
            case SensorManager.SENSOR_DELAY_FASTEST:
                title = R.string.menu_delay_fastest;
                break;
            case SensorManager.SENSOR_DELAY_GAME:
                title = R.string.menu_delay_game;
                break;
            case SensorManager.SENSOR_DELAY_UI:
                title = R.string.menu_delay_ui;
                break;
            case SensorManager.SENSOR_DELAY_NORMAL:
                title = R.string.menu_delay_normal;
                break;
        }
        menu.findItem(R.id.menu_delay).setTitle(title);
        menu.findItem(R.id.menu_accelerometer).setEnabled(type != Sensor.TYPE_ACCELEROMETER);
        menu.findItem(R.id.menu_gyroscope).setEnabled(type != Sensor.TYPE_GYROSCOPE);
        menu.findItem(R.id.menu_magnetic_field).setEnabled(type != Sensor.TYPE_MAGNETIC_FIELD);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.menu_delay:
                Log.d(TAG, "menu_delay");
                int index = DELAYS.indexOf(delay);
                delay = DELAYS.get((index + 1) % DELAYS.size());
                break;
            case R.id.menu_accelerometer:
                type = Sensor.TYPE_ACCELEROMETER;
                typeView.setText(R.string.menu_accelerometer);
                break;
        }
        invalidateOptionsMenu();
        changeConfig();
        return super.onOptionsItemSelected(item);
    }

    private void changeConfig() {
        manager.unregisterListener(this);
        Sensor sensor = manager.getDefaultSensor(type);
        manager.registerListener(this, sensor, delay);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long ts = event.timestamp;
        rate = (int) (ts - prevTimestamp) / 1000;

        final float NS2S = 1.0f / 1000000000.0f;
        float dT = (ts - prevTimestamp)*NS2S;
        time+=dT;

        float rax = event.values[0];
        float ray = event.values[1];
        float raz = event.values[2];

        if (prevTimestamp==0){//fist step
            dT=0;//initial dT reset
            //detecting initial position
            if (rax<-9){
                GravF=1;
                Grav=rax;
            }else if(ray<-9){
                GravF=2;
                Grav=ray;
            }else if(raz<-9){
                GravF=3;
                Grav=raz;
            }else if (rax>9){
                GravF=4;
                Grav=rax;
            }else if(ray>9){
                GravF=5;
                Grav=ray;
            }else if(raz>9){
                GravF=6;
                Grav=raz;
            }
            time = 0;
        }

        if (GravF==1){
            rax+=Grav;
        }else if(GravF==2){
            ray+=Grav;
        }else if(GravF==3){
            raz+=Grav;
        }else if (GravF==4){
            rax-=Grav;
        }else if(GravF==5){
            ray-=Grav;
        }else if(GravF==6){
            raz-=Grav;
        }
        rA = (float) Math.sqrt(rax * rax + ray * ray + raz * raz);


        rvx+=(rax +oldrax)/2*dT;
        Log.d(TAG, "onSensorChanged: "+ rax +" + "+ oldrax +" /2 * "+dT+" = "+ (rax +oldrax)/2*dT);
        rvx/=shV;
        rvy+=(ray +oldray)/2*dT;
        rvy/=shV;
        rvz+=(raz +oldraz)/2*dT;
        rvz/=shV;
        rV = (float) Math.sqrt(rvx*rvx+rvy*rvy+vvz*rvz);
        oldrax= rax;
        oldray= ray;
        oldraz= raz;

        rdx+=(rvx+oldrvx)/2*dT;
        rdx/=shD;
        rdy+=(rvy+oldrvy)/2*dT;
        rdy/=shD;
        rdz+=(rvz+oldrvz)/2*dT;
        rdz/=shD;
        rD = (float) Math.sqrt(rdx*rdx+rdy*rdy+rdz*rdz);
        oldrvx=rvx;
        oldrvy=rvy;
        oldrvz=rvz;

        Log.i(TAG, "Ax=" + rax + ", Ay=" + ray + ", Az=" + raz + ", A=" + rA);
        Log.i(TAG, "Vx=" + rvx + ", Vy=" + rvy + ", Vz=" + rvz + ", V=" + rV);
        Log.i(TAG, "Dx=" + rdx + ", Dy=" + rdy + ", Dz=" + rdz + ", D=" + rD);

        vax = ALPHA * vax + (1 - ALPHA) * rax;
        vay = ALPHA * vay + (1 - ALPHA) * ray;
        vaz = ALPHA * vaz + (1 - ALPHA) * raz;
        vA = (float) Math.sqrt(vax*vax+vay*vay+vaz*vaz);

        vvx=(vax+oldvax)/2*dT+vvx;
        vvx/=shV;
        vvy=(vay+oldvay)/2*dT+vvy;
        vvy/=shV;
        vvz=(vaz+oldvaz)/2*dT+vvz;
        vvz/=shV;
        vV = (float) Math.sqrt(vvx*vvx+vvy*vvy+vvz*vvz);
        oldvax=vax;
        oldvay=vay;
        oldvaz=vaz;

        vdx=(vvx+oldvvx)/2*dT+vdx;
        vdx/=shD;
        vdy=(vvy+oldvvy)/2*dT+vdy;
        vdy/=shD;
        vdz=(vvz+oldvvz)/2*dT+vdz;
        vdz/=shD;
        vD = (float) Math.sqrt(vdx*vdx+vdy*vdy+vdz*vdz);
        oldvvx=vvx;
        oldvvy=vvy;
        oldvvz=vvz;

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
}
