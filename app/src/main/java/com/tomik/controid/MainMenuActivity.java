package com.tomik.controid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
        prevAccel = null;
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
    float[] prevAccel = null;
    float[] velocity = null;
    float[] position = null;
    float[] accel = null;
    boolean calibrationPressed = true;
    float xCalibratedFix = 0;
    float yCalibratedFix = 0;
    float zCalibratedFix = 0;
    long last_timestamp = 0;
    private float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private final float accelKFactor = (float) 0.9;

    public int IdxAll = 0;
    public final int IdxAllMax = 100000;
    public float[][] AllReadingsAccel = new float[IdxAllMax][3];
    public float[][] AllReadingsVel = new float[IdxAllMax][3];
    public float[][] AllReadingsPos = new float[IdxAllMax][3];
    public float[][] AllReadingsRot = new float[IdxAllMax][3];

    public int X=0;
    public int Y=1;
    public int Z=2;
    private int maxMeasur = 8;
    private int lastTmp = 0;
    public float[][] LastMeasur = new float[maxMeasur][3];

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

                if (prevAccel == null) {
                    prevAccel = new float[3];
                    accel = new float[3];
                    velocity = new float[3];
                    position = new float[3];
                    prevAccel[0] = prevAccel[1] = prevAccel[2] = 0f;
                    velocity[0] = velocity[1] = velocity[2] = 0f;
                    position[0] = position[1] = position[2] = 0f;
                    break;
                }

                float dt = (event.timestamp - last_timestamp) * NS2S;
                last_timestamp = event.timestamp;
                if (calibrationPressed) {
                    Calibrate(event.values, dt);
                    IdxAll = 0;
                    break;
                }

                for (int index = 0; index < 3; ++index) {
                    accel[index] = event.values[index];

                    //eliminacja drgań
                    if (Math.abs(accel[index])<1.2)
                        accel[index]=0;
                    //if (Math.abs(accel[index])>10)
                    //    accel[index]=10;
                    AllReadingsAccel[IdxAll % IdxAllMax][index] = accel[index];

                    float last_velocity = velocity[index];
                    velocity[index] += (accel[index] + prevAccel[index]) / 2 * dt;
                    AllReadingsVel[IdxAll % IdxAllMax][index] = velocity[index];
                    position[index] += (velocity[index] + last_velocity) / 2 * dt;
                    AllReadingsPos[IdxAll % IdxAllMax][index] = position[index];
                    prevAccel[index] = accel[index];
                }
                IdxAll++;
                SensorLinearAccelerationTV.setText(String.format("%.2f", position[0]) + " " +
                        String.format("%.2f", position[1]) + " " +
                        String.format("%.2f", position[2]));


                for (int i=0;i<lastTmp;i++)
                    for (int j=0;j<3;j++)
                        LastMeasur[i][j] = LastMeasur[i+1][j];
                for (int j=0;j<3;j++)
                    LastMeasur[lastTmp][j] = accel[j];
                if (lastTmp<maxMeasur-1) lastTmp++;
                DetectMoves();

                break;
                case Sensor.TYPE_GYROSCOPE:
                    break;
            default:
                break;
        }
        // Do something with this sensor value.
    }

    private void DetectMoves() {
        if (checkForJump())
            Log.i("SKOK","!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    private final int jumpCooldown = 4;
    private int lastJump=0;
    private boolean checkForJump() {
        if (lastJump>0) {
            lastJump--;
            return false;
        }
        if ((LastMeasur[maxMeasur-1][Y]>6 || LastMeasur[maxMeasur-2][Y]>6) && (Math.abs(LastMeasur[maxMeasur-3][Y])<2.5 || LastMeasur[maxMeasur-2][Y]<2.5) &&
                !(LastMeasur[maxMeasur-2][Z]<-5.5 || LastMeasur[maxMeasur-3][Z]<-5.5)) {
            lastJump = jumpCooldown;
            return true;
        }
        return false;
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
        prevAccel = null;
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
        //mSensorManager.unregisterListener(this);
    }

    public void Calibration(View view) {
        calibrationPressed = true;
        xCal = 0;
        yCal = 0;
        zCal = 0;
        calibrationTime = 0;
    }
}