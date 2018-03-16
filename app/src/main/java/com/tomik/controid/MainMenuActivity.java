package com.tomik.controid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainMenuActivity extends AppCompatActivity implements SensorEventListener {

    public TextView SensorLinearAccelerationTV;
    //public TextView SensorSignificantMotionTV;
    private SensorManager mSensorManager;
    private Sensor mLinearAcceleration;
    //private Sensor mRotationVector;
    //private Sensor mMagneticField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


        SensorLinearAccelerationTV = findViewById(R.id.AccelerometerTextID);
        //SensorSignificantMotionTV = findViewById(R.id.SignificantMotionTextID);
        //do sensorów
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //inicjalizacja czujników
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        //mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    static final float NS2S = 1.0f / 1000000000.0f;
    float[] last_values = null;
    float[] velocity = null;
    float[] position = null;
    float[] acceleration = null;
    boolean calibrationPressed = false;
    float xCalibratedFix = 0;
    float yCalibratedFix = 0;
    float zCalibratedFix = 0;
    long last_timestamp = 0;
    private float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private int idxAll=0;
    private final int idxAllMax=100000;
    private float[][] allReadings = new float[idxAllMax][3];

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        switch (event.sensor.getType()) {
            /*case Sensor.TYPE_ACCELEROMETER:
                if (mMagnetometerReading==null)
                    break;
                SensorManager.getRotationMatrix(mRotationMatrix, null,
                        event.values, mMagnetometerReading);

                // "mRotationMatrix" now has up-to-date information.

                SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

                SensorSignificantMotionTV.setText(String.format("%.2f", mOrientationAngles[0]) + " " +
                        String.format("%.2f", mOrientationAngles[1]) + " " +
                        String.format("%.2f", mOrientationAngles[2]));
                break;*/
            case Sensor.TYPE_LINEAR_ACCELERATION:

                if (last_values == null) {
                    last_values = new float[3];
                    acceleration = new float[3];
                    velocity = new float[3];
                    position = new float[3];
                    velocity[0] = velocity[1] = velocity[2] = 0f;
                    position[0] = position[1] = position[2] = 0f;
                    break;
                }

                float dt = (event.timestamp - last_timestamp) * NS2S;
                last_timestamp = event.timestamp;
                if (calibrationPressed) {
                    Calibrate(event.values, dt);
                    idxAll=0;
                    break;
                }
                //pomysły
                //znajdować dłuższy ruch jednostajny
                //wychwytywać gwałtowną zmianę zwrotu

                for (int index = 0; index < 3; ++index) {
                    acceleration[index] = event.values[index];
                    allReadings[idxAll%idxAllMax][index] = event.values[index];
                    //eliminacja drgań
                    if (Math.abs(acceleration[index])<0.006)
                        acceleration[index]=0;
                    if (Math.abs(acceleration[index])>10)
                        acceleration[index]=10;

                    float last_velocity = velocity[index];
                    velocity[index] += (acceleration[index] + last_values[index]) / 2 * dt;
                    //jak jednostajny to znaczy, że prawd. stoi
                    if (Math.abs(last_velocity-velocity[index])<0.06)
                        velocity[index]=0;
                    //hamowanie
                    /*if (velocity[index]>0) {
                        velocity[index] -= 0.01;
                        if (velocity[index] <= 0)
                            velocity[index] = 0;
                    }
                    else {
                        velocity[index] += 0.01;
                        if (velocity[index] >= 0)
                            velocity[index] = 0;
                    }*/
                    position[index] += (velocity[index] + last_velocity) / 2 * dt;
                    last_values[index] = acceleration[index];
                }
                idxAll++;
                SensorLinearAccelerationTV.setText(String.format("%.2f", position[0]) + " " +
                        String.format("%.2f", position[1]) + " " +
                        String.format("%.2f", position[2]));
                break;
            default:
                break;
        }
        // Do something with this sensor value.
    }

    float xCal = 0;
    float yCal = 0;
    float zCal = 0;
    float calibrationTime = 0;
    final float calibrationTimeLimit = 10;

    private void Calibrate(float[] acceleration, float dt) {
        xCal += acceleration[0];
        yCal += acceleration[1];
        zCal += acceleration[2];
        calibrationTime += dt;
        //if (calibrationTime < calibrationTimeLimit)
        //    return;
        calibrationPressed = false;
        xCalibratedFix = xCal / calibrationTime;
        yCalibratedFix = yCal / calibrationTime;
        zCalibratedFix = zCal / calibrationTime;
        last_values=null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);

        //mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //TODO jeśli po wyłączeniu ekranu nie będzie zbierało danych to przez tą funkcję
        mSensorManager.unregisterListener(this);
    }

    public void Calibration(View view) {
        calibrationPressed = true;
        xCal = 0;
        yCal = 0;
        zCal = 0;
        calibrationTime = 0;
    }
}
