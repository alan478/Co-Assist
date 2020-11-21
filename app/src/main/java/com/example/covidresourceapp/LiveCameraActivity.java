package com.example.covidresourceapp;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;

import java.io.IOException;

public class LiveCameraActivity extends Activity implements TextureView.SurfaceTextureListener {
    public double height = 5.8;
    private View view;

    private Camera mCamera;
    private TextureView mTextureView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_detection);
        mTextureView = findViewById(R.id.my_textureView);
        mTextureView.setSurfaceTextureListener(this);
        view = findViewById(R.id.view);
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        float[] mGravity;
        float[] mGeomagnetic;
        public double pitch;
        public boolean outside_six_feet = false;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientationData[] = new float[3];
                    SensorManager.getOrientation(R, orientationData);

                    pitch = orientationData[1];
                    if (height / Math.cos(pitch) >= 6) {
                        outside_six_feet = true;
                    } else {
                        outside_six_feet = false;
                    }
                    if(outside_six_feet){
                        //set overlay green
                        view.setBackgroundColor(0x4004FC0A);
                    }
                    else{
                        view.setBackgroundColor(0x40FF3200);
                    }
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}