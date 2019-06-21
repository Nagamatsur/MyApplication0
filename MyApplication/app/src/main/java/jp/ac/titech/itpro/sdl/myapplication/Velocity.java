package jp.ac.titech.itpro.sdl.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class Velocity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = SensorActivity.class.getSimpleName();
    private static final int MAX_VALUES_SIZE = 8;
    public static final String EXTRA_SENSOR_POS = "sensor_pos";

    private TextView nameView;
    private TextView typeView;
    private TextView typeStringView;
    private TextView vendorView;
    private TextView delayView;
    private TextView accuracyView;
    private TextView timestampView;
    private TextView dataSizeView;
    private TextView[] valueViews;

    private SensorManager manager;
    private Sensor sensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_sensor);

        nameView = findViewById(R.id.sensor_name);
        typeView = findViewById(R.id.sensor_type);
        typeStringView = findViewById(R.id.sensor_type_string);
        vendorView = findViewById(R.id.sensor_vendor);
        delayView = findViewById(R.id.sensor_delay);
        accuracyView = findViewById(R.id.sensor_accuracy);
        timestampView = findViewById(R.id.sensor_timestamp);
        dataSizeView = findViewById(R.id.sensor_data_size);
        valueViews = new TextView[MAX_VALUES_SIZE];
        valueViews[0] = findViewById(R.id.sensor_value0);
        valueViews[1] = findViewById(R.id.sensor_value1);
        valueViews[2] = findViewById(R.id.sensor_value2);
        valueViews[3] = findViewById(R.id.sensor_value3);
        valueViews[4] = findViewById(R.id.sensor_value4);
        valueViews[5] = findViewById(R.id.sensor_value5);
        valueViews[6] = findViewById(R.id.sensor_value6);
        valueViews[7] = findViewById(R.id.sensor_value7);

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (manager == null) {
            Toast.makeText(this, R.string.toast_no_sensor_manager, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        List<Sensor> sensors = SensorUtil.getSensors(manager);
        int pos = getIntent().getIntExtra(EXTRA_SENSOR_POS, -1);
        if (0 <= pos && pos < sensors.size()) {
            sensor = sensors.get(pos);
        } else {
            Toast.makeText(this, R.string.toast_no_sensor, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        nameView.setText(sensor.getName());
        typeView.setText(SensorUtil.getTypeName(sensor));
        typeStringView.setText(sensor.getStringType());
        vendorView.setText(sensor.getVendor());
        delayView.setText(getString(R.string.delay_format, sensor.getMinDelay(), sensor.getMaxDelay()));
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        accuracyView.setText(getString(R.string.int_format, event.accuracy));
        timestampView.setText(getString(R.string.long_format, event.timestamp));
        dataSizeView.setText(getString(R.string.int_format, event.values.length));
        int n = Math.min(event.values.length, MAX_VALUES_SIZE);
        for (int i = 0; i < n; i++) {
            valueViews[i].setText(getString(R.string.float_format, event.values[i]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        accuracyView.setText(getString(R.string.int_format, accuracy));
    }
}
