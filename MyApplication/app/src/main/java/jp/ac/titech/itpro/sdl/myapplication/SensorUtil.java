package jp.ac.titech.itpro.sdl.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

class SensorUtil {
    static String getTypeName(Sensor sensor) {
        try {
            Class klass = sensor.getClass();
            for (Field field : klass.getFields()) {
                String fieldName = field.getName();
                if (fieldName.startsWith("TYPE_") && field.getInt(klass) == sensor.getType()) {
                    return fieldName;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    static List<Sensor> getSensors(SensorManager manager) {
        List<Sensor> result = new ArrayList<>();
        for (Sensor sensor : manager.getSensorList(Sensor.TYPE_ALL)) {
            if (getTypeName(sensor) != null) {
                result.add(sensor);
            }
        }
        return result;
    }
}
