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

import java.util.LinkedList;

public class MainMenuActivity extends AppCompatActivity implements SensorEventListener {

    public TextView SensorLinearAccelerationTV;
    public TextView SensorSignificantMotionTV;
    private SensorManager mSensorManager;
    private Sensor mLinearAcceleration;
    private Sensor mGameVector;
    private long delay = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SensorLinearAccelerationTV = findViewById(R.id.AccelerometerTextID);
        SensorSignificantMotionTV = findViewById(R.id.SignificantMotionTextID);
        //do sensorów
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //inicjalizacja czujników
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGameVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        for (int i = 0; i < maxMeasur; i++) {
            LastRotation.add(new Float[]{0f, 0f, 0f});
            LastMeasur.add(new Float[]{0f, 0f, 0f});
        }
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

    float[] accel = new float[3];
    float[] accelOrg = new float[3];
    float[] rotation = new float[3];
    float[] startRotation = new float[3];
    boolean calibrationPressed = true;
    private final float[] mRotationMatrix = new float[9];

    public int IdxAll = 0;
    public final int IdxAllMax = 100000;
    public float[][] AllReadingsAccel = new float[IdxAllMax][3];
    public float[][] AllReadingsRot = new float[IdxAllMax][3];

    public int X = 0;
    public int Y = 1;
    public int Z = 2;
    private int maxMeasur = 8;
    private int lastTmp = 0;
    private boolean useRotation = false;
    public LinkedList<Float[]> LastMeasur = new LinkedList<>();
    public LinkedList<Float[]> LastRotation = new LinkedList<>();

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public final void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION: {
                System.arraycopy(event.values, 0, accelOrg, 0, 3);

                if (!useRotation) {
                    System.arraycopy(accelOrg, 0, accel, 0, 3);
                    if (rotation[Y] > 0) accel[Y] *= -1;
                } else {
                    accel[X] = (float) (accelOrg[X] * (Math.cos(rotation[X]) * Math.cos(rotation[Z])) +
                            accelOrg[Y] * (Math.sin(rotation[Y]) * Math.sin(rotation[X])) +
                            accelOrg[Z] * (-Math.sin(rotation[X]) * Math.cos(rotation[Y])) +
                            accelOrg[Z] * (-Math.sin(rotation[X]) * Math.cos(rotation[Z])));
                    accel[Y] = (float) (accelOrg[X] * (Math.sin(rotation[Y]) * -Math.sin(rotation[Z])) +
                            accelOrg[Y] * Math.cos(rotation[Y]) +
                            accelOrg[Z] * (Math.sin(rotation[Y]) * Math.cos(rotation[Z])));
                    accel[Z] = (float) (accelOrg[X] * -Math.sin(rotation[X]) * Math.cos(rotation[Z]) +
                            accelOrg[Y] * Math.sin(rotation[Y]) * Math.cos(rotation[X]) +
                            accelOrg[Z] * Math.cos(rotation[X]) * Math.cos(rotation[Z]) +
                            accelOrg[Z] * -Math.sin(rotation[X]) * Math.cos(rotation[Z]));
                }

                float sum = 0;
                for (float v : accelOrg) sum += v;
                for (float v : accel) sum -= v;

                for (int index = 0; index < accel.length; ++index)
                    if (Math.abs(accel[index]) < 1.2)
                        accel[index] = 0;

                System.arraycopy(accel, 0, AllReadingsAccel[IdxAll % IdxAllMax], 0, accel.length);
                System.arraycopy(rotation, 0, AllReadingsRot[IdxAll % IdxAllMax], 0, rotation.length);
                IdxAll++;

                //zapisz ostatnie 10 odczytów (używane do analizy zdarzeń)
                LastRotation.add(new Float[]{rotation[X], rotation[Y], rotation[Z]});
                LastMeasur.add(new Float[]{accel[X], accel[Y], accel[Z]});
                LastRotation.removeFirst();
                LastMeasur.removeFirst();
                //wykryj zdarzenia
                DetectMoves();

                //wypisanie na ekran
                SensorLinearAccelerationTV.setText(String.format("%+.2f", accel[X]) + " " +
                        String.format("%+.2f", accel[Y]) + " " +
                        String.format("%+.2f", accel[Z]) + " " +
                        String.format("%+.2f", sum));
                break;
            }
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                if (rotation == null)
                    break;
                if (delay > 0)
                    calibrationPressed = true;
                updateOrientation(event.values);
                SensorSignificantMotionTV.setText(String.format("%+.2f", rotation[X]) + " " +
                        String.format("%+.2f", rotation[Y]) + " " +
                        String.format("%+.2f", rotation[Z]));
                break;
            default:
                break;
        }
    }

    private void updateOrientation(float[] values) {
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, values);
        SensorManager.getOrientation(mRotationMatrix, rotation);
        if (calibrationPressed) {
            //System.arraycopy(rotation, 0, startRotation, 0, rotation.length);
            for (int i = 0; i < 3; i++)
                startRotation[i] = 0;
            calibrationPressed = false;
            Log.i("ZAKTUALIZOWANO ROTACJĘ", "!!!!!!!!!!!!!!!!!!!!!");
            delay--;
        }
        for (int i = 0; i < rotation.length; i++) {
            rotation[i] -= startRotation[i];
            if (rotation[i] > Math.PI)
                rotation[i] -= Math.PI * 2;
            if (rotation[i] <= -Math.PI)
                rotation[i] += Math.PI * 2;
        }
    }

    private void DetectMoves() {
        if (checkForJump())
            Log.i("SKOK", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (checkForCrouch())//skok przed kucaniem
            Log.i("KUCNIĘCIE", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (checkForLeft())
            Log.i("LEWO", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (checkForRight())
            Log.i("PRAWO", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    private boolean checkForRight() {
        return checkForSide(false);
    }

    private boolean checkForLeft() {
        return checkForSide(true);
    }

    private final int sideCooldown = 4;
    private int lastSide = 0;

    private boolean checkForSide(boolean sign) {
        if (lastJump > 0 || lastCrouch > 0)
            return false;
        if (lastSide > 0) {
            lastSide--;
            return false;
        }
        int s;
        if (sign)
            s = -1;
        else
            s = 1;
        if (LastMeasur.get(maxMeasur - 1)[X] * s > 2.5 && LastMeasur.get(maxMeasur - 2)[X] * s < 2.5)
        //Math.abs(LastMeasur[maxMeasur - 2][X]) == 0)
        //&& !(LastMeasur[maxMeasur-2][X]<-5.5 || LastMeasur[maxMeasur-3][X]<-5.5))
        {
            lastSide = sideCooldown;
            return true;
        }
        return false;
    }

    private final int crouchCooldown = 6;
    private int lastCrouch = 0;

    private boolean checkForCrouch() {
        if (lastJump > 0)
            return false;
        if (lastCrouch > 0) {
            lastCrouch--;
            return false;
        }
        if (RotationChange() < 0.65) return false;
        lastCrouch = crouchCooldown;
        return true;
    }

    private final int jumpCooldown = 4;
    private int lastJump = 0;

    private boolean checkForJump() {
        if (lastJump > 0) {
            lastJump--;
            return false;
        }
        if (Math.abs(LastMeasur.get(maxMeasur - 1)[Y]) < 4.5 && Math.abs(LastMeasur.get(maxMeasur - 2)[Y]) < 4.5)
            return false;
        lastJump = jumpCooldown;
        return true;
    }

    float RotationChange() {
        float minY = 1000;
        float maxY = -1;
        for (int i = LastRotation.size() - 1; LastRotation.size() - 1 - i < 4; i--) {
            minY = Math.min(minY, LastRotation.get(i)[Y]);
            maxY = Math.max(maxY, LastRotation.get(i)[Y]);
        }
        return Math.abs(maxY - minY);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGameVector, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //TODO jeśli po wyłączeniu ekranu nie będzie zbierało danych to przez tą funkcję
        //mSensorManager.unregisterListener(this);
    }

    public void Calibration(View view) {
        calibrationPressed = true;
    }
}