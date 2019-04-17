package com.video.player.lib.listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * TinyHung@Outlook.com
 * 2019/4/10
 * Screen Orientation Listener
 */

public class VideoOrientationListener implements SensorEventListener {

    private static final String TAG = "VideoOrientationListener";
    //设备旋转的角度，依次顺时针角度
    private int mOrientation = 0;
    private OnOrientationChangeListener mListener;

    public VideoOrientationListener(OnOrientationChangeListener listener){
        this.mListener=listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
            return;
        }
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        int newOrientationAngle;
        if (x < 4.5 && x >= -4.5 && y >= 4.5) {
            newOrientationAngle = 0;
        } else if (x >= 4.5 && y < 4.5 && y >= -4.5) {
            newOrientationAngle = 270;
        } else if (x <= -4.5 && y < 4.5 && y >= -4.5) {
            newOrientationAngle = 90;
        }else {
            newOrientationAngle = 180;
        }
        if (newOrientationAngle!=mOrientation) {
            if (mListener != null) {
                mListener.orientationChanged(newOrientationAngle);
            }
            mOrientation = newOrientationAngle;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public interface OnOrientationChangeListener {
        void orientationChanged(int newOrientation);
    }
}